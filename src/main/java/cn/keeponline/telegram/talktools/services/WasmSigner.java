package cn.keeponline.telegram.talktools.services;

import cn.keeponline.telegram.talktools.logging.Logging;
import io.github.kawamuray.wasmtime.*;
import io.github.kawamuray.wasmtime.Module;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;

/**
 * WASM 签名器
 * 基于 Python 版本的实现，使用 wasmtime-java 调用 WASM 模块
 */
public class WasmSigner {
    private static final Logger logger = Logging.getLogger(WasmSigner.class);
    private static final String WASM_FILE_NAME = "hmac_signer_bg.c51da387c65b1674ac48.wasm";
    
    private Engine engine;
    private Store<Void> store;
    private Instance instance;
    private Memory memory;
    private Func mallocFunc;
    private Func freeFunc;
    private Func genSignatureFunc;
    private boolean initialized = false;
    
    /**
     * 初始化 WASM 模块
     */
    public synchronized void init() {
        if (initialized) {
            return;
        }
        
        try {
            Path wasmPath = findWasmFile();
            if (wasmPath == null || !Files.exists(wasmPath)) {
                logger.error("未找到 WASM 文件: {}", WASM_FILE_NAME);
                throw new RuntimeException("WASM 文件不存在: " + WASM_FILE_NAME);
            }
            
            // 创建 Engine 和 Store（这里可能会触发 JNI 库加载）
            try {
                engine = new Engine();
                store = new Store<>(null, engine);
            } catch (UnsatisfiedLinkError e) {
                // 检查是否是 GLIBC 版本问题
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("GLIBC")) {
                    String requiredVersion = extractRequiredGlibcVersion(errorMsg);
                    String friendlyError = String.format(
                        "WASM 初始化失败：系统 GLIBC 版本不兼容。\n" +
                        "当前系统可能缺少 GLIBC %s 或更高版本。\n" +
                        "错误详情: %s\n" +
                        "解决方案：\n" +
                        "1. 升级系统 GLIBC（不推荐，可能影响系统稳定性）\n" +
                        "2. 使用 Docker 容器运行（推荐）：使用包含 GLIBC 2.25+ 的基础镜像（如 Ubuntu 18.04+, CentOS 8+, Debian 10+）\n" +
                        "3. 在更高版本的 Linux 发行版上运行",
                        requiredVersion != null ? requiredVersion : "2.25",
                        errorMsg
                    );
                    logger.error(friendlyError);
                    throw new RuntimeException(friendlyError, e);
                }
                // 其他 UnsatisfiedLinkError
                throw new RuntimeException("WASM 初始化失败：无法加载本地库。请检查系统依赖是否完整。", e);
            }
            
            // 加载 WASM 模块
            byte[] wasmBytes = Files.readAllBytes(wasmPath);
            Module module = Module.fromBinary(engine, wasmBytes);
            
            // 为所有 import 创建简单的 stub（大部分 wasm-bindgen 的辅助函数不会在我们的路径上用到）
            List<Extern> imports = new ArrayList<>();
            for (ImportType importType : module.imports()) {
                switch (importType.type()) {
                    case FUNC: {
                        FuncType funcType = importType.func();
                        Func stubFunc = new Func(store, funcType, (caller, params, results) -> {
                            // 占位函数：返回默认值
                            for (int i = 0; i < results.length; i++) {
                                results[i] = Val.fromI32(0);
                            }
                            return java.util.Optional.empty();
                        });
                        imports.add(Extern.fromFunc(stubFunc));
                        break;
                    }
                    case MEMORY: {
                        MemoryType memType = importType.memory();
                        Memory mem = new Memory(store, memType);
                        imports.add(Extern.fromMemory(mem));
                        break;
                    }
                    default:
                        throw new RuntimeException("Unsupported wasm import type: " + importType.type());
                }
            }

            // 创建实例
            instance = new Instance(store, module, imports);

            // 获取导出的函数和内存
            memory = instance.getMemory(store, "memory")
                    .orElseThrow(() -> new RuntimeException("WASM 内存导出缺失"));
            mallocFunc = instance.getFunc(store, "__wbindgen_malloc")
                    .orElseThrow(() -> new RuntimeException("WASM 导出缺失: __wbindgen_malloc"));
            freeFunc = instance.getFunc(store, "__wbindgen_free")
                    .orElseThrow(() -> new RuntimeException("WASM 导出缺失: __wbindgen_free"));
            genSignatureFunc = instance.getFunc(store, "gen_signature")
                    .orElseThrow(() -> new RuntimeException("WASM 导出缺失: gen_signature"));
            
            initialized = true;
            logger.info("WASM 模块初始化成功");
        } catch (Exception e) {
            logger.error("初始化 WASM 失败", e);
            throw new RuntimeException("WASM 初始化失败", e);
        }
    }
    
    /**
     * 从错误信息中提取所需的 GLIBC 版本
     */
    private String extractRequiredGlibcVersion(String errorMsg) {
        if (errorMsg == null) {
            return null;
        }
        // 匹配类似 "version `GLIBC_2.25'" 的模式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("GLIBC_([0-9]+\\.[0-9]+)");
        java.util.regex.Matcher matcher = pattern.matcher(errorMsg);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * 查找 WASM 文件路径
     */
    private Path findWasmFile() {
        // 1. 从 resources 目录查找（支持 jar 包内资源）
        try (InputStream is = getClass().getResourceAsStream("/assets/" + WASM_FILE_NAME)) {
            if (is != null) {
                try {
                    URI resourceUri = getClass().getResource("/assets/" + WASM_FILE_NAME).toURI();
                    
                    // 如果是 jar 包内的资源（jar:file:...），需要提取到临时文件
                    if (resourceUri.getScheme().equals("jar")) {
                        // 提取到临时文件
                        Path tempFile = Files.createTempFile("wasm_", "_" + WASM_FILE_NAME);
                        tempFile.toFile().deleteOnExit(); // JVM 退出时删除临时文件
                        
                        // 从 InputStream 复制到临时文件
                        Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        
                        logger.info("从 jar 包内提取 WASM 文件到临时目录: {}", tempFile);
                        return tempFile;
                    } else {
                        // 普通文件系统路径，直接返回
                        return Paths.get(resourceUri);
                    }
                } catch (Exception e) {
                    logger.warn("无法从 resources 加载 WASM 文件", e);
                }
            }
        } catch (Exception e) {
            logger.warn("无法打开 resources 流", e);
        }
        
        // 2. 从项目根目录的 assets 目录查找
        Path projectPath = Paths.get(System.getProperty("user.dir"));
        Path assetsPath = projectPath.resolve("assets").resolve(WASM_FILE_NAME);
        if (Files.exists(assetsPath)) {
            return assetsPath;
        }
        
        // 3. 从相对路径查找
        Path relativePath = Paths.get("assets", WASM_FILE_NAME);
        if (Files.exists(relativePath)) {
            return relativePath;
        }
        
        return null;
    }
    
    /**
     * 在 WASM 内存中分配字符串
     * 对应 Python 的 _wasm_alloc_str
     */
    private long[] wasmAllocStr(String s) {
        if (s == null || s.isEmpty()) {
            return new long[]{0, 0};
        }
        
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        int length = bytes.length;
        
        try {
            long ptr;
            try {
                // 常见签名：fn __wbindgen_malloc(size: usize) -> *mut u8
                Val[] result = mallocFunc.call(store, Val.fromI32(length));
                ptr = result[0].i32() & 0xFFFFFFFFL;
            } catch (Exception ex) {
                // 兼容部分构建：fn __wbindgen_malloc(size: usize, align: usize) -> *mut u8
                Val[] result = mallocFunc.call(store, Val.fromI32(length), Val.fromI32(1));
                ptr = result[0].i32() & 0xFFFFFFFFL;
            }
            
            // 写入内存
            ByteBuffer buffer = memory.buffer(store);
            buffer.position((int) ptr);
            buffer.put(bytes);
            
            return new long[]{ptr, length};
        } catch (Exception e) {
            logger.error("WASM malloc 失败", e);
            throw new RuntimeException("WASM malloc 失败", e);
        }
    }
    
    /**
     * 从 WASM 内存读取字符串并释放
     * 对应 Python 的 _wasm_read_str
     */
    private String wasmReadStr(long ptr, int length) {
        if (ptr == 0 || length == 0) {
            return "";
        }
        
        try {
            // 读取内存
            byte[] bytes = new byte[length];
            ByteBuffer buffer = memory.buffer(store);
            buffer.position((int) ptr);
            buffer.get(bytes, 0, length);
            
            try {
                // 常见签名：fn __wbindgen_free(ptr: *mut u8, size: usize, align: usize)
                freeFunc.call(store, Val.fromI32((int) ptr), Val.fromI32(length), Val.fromI32(1));
            } catch (Exception ex) {
                // 兼容部分构建：fn __wbindgen_free(ptr: *mut u8, size: usize)
                freeFunc.call(store, Val.fromI32((int) ptr), Val.fromI32(length));
            }
            
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("WASM read string 失败", e);
            throw new RuntimeException("WASM read string 失败", e);
        }
    }
    
    /**
     * 生成签名
     * 对应 Python 的 sign 方法
     * 
     * @param a 参与签名的第一个参数
     * @param timestamp 时间戳
     * @param nonce 随机数
     * @param token 令牌（可选）
     * @return 签名字符串
     */
    public String sign(String a, String timestamp, String nonce, String token) {
        if (!initialized) {
            init();
        }
        
        try {
            // 分配字符串到 WASM 内存
            long[] ptrA = wasmAllocStr(a);
            long[] ptrTs = wasmAllocStr(timestamp);
            long[] ptrNonce = wasmAllocStr(nonce);
            long[] ptrTok = wasmAllocStr(token != null ? token : "");
            
            // 调用 gen_signature 函数
            // 参数：ptr_a, len_a, ptr_ts, len_ts, ptr_nonce, len_nonce, ptr_tok, len_tok
            Val[] result = genSignatureFunc.call(store, new Val[]{
                Val.fromI32((int) ptrA[0]),
                Val.fromI32((int) ptrA[1]),
                Val.fromI32((int) ptrTs[0]),
                Val.fromI32((int) ptrTs[1]),
                Val.fromI32((int) ptrNonce[0]),
                Val.fromI32((int) ptrNonce[1]),
                Val.fromI32((int) ptrTok[0]),
                Val.fromI32((int) ptrTok[1])
            });
            
            // result expected to be a 2-element array [ptr, len]
            long outPtr = result[0].i32() & 0xFFFFFFFFL;
            int outLen = result[1].i32();
            
            // 读取结果字符串
            String signature = wasmReadStr(outPtr, outLen);
            
            logger.debug("sign inputs: a={}, timestamp={}, nonce={}, token={}, signature={}",
                    a, timestamp, nonce, token, signature);
            
            return signature;
        } catch (Exception e) {
            logger.error("WASM 签名失败", e);
            throw new RuntimeException("签名失败", e);
        }
    }
}
