package cn.keeponline.telegram.utils;

public class StringReplaceExample {

    public static void main(String[] args) {
        // 你的原始字符串
        String originalString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n" +
                "<plist version=\"1.0\">\n" +
                "<dict>\n" +
                "\t<key>PayloadContent</key>\n" +
                "\t<array>\n" +
                "\t\t<dict>\n" +
                "\t\t\t<key>FullScreen</key>\n" +
                "\t\t\t<true/>\n" +
                "\t\t\t<key>Icon</key>\n" +
                "\t\t\t<!-- APP桌面图标 base64格式（宽高200px，删除“data:image/png;base64,”或“data:image/jpeg;base64,”），不能为空，需要修改 -->\n" +
                "\t\t\t<data>\n" +
                "\t\t\tXXX\n" +
                "\t\t\t</data>\n" +
                "\t\t\t<key>IsRemovable</key>\n" +
                "\t\t\t<true/>\n" +
                "\t\t\t<key>Label</key>\n" +
                "\t\t\t<!-- APP桌面名称，需要修改 -->\n" +
                "\t\t\t<string>百度</string>\n" +
                "\t\t\t<key>PayloadIdentifier</key>\n" +
                "\t\t\t<!-- 标识符 每个平台不能相同，需要修改 -->\n" +
                "\t\t\t<string>abc.com</string>\n" +
                "\t\t\t<key>PayloadOrganization</key>\n" +
                "\t\t\t<string>授权安装进入下一步</string>\n" +
                "\t\t\t<key>PayloadType</key>\n" +
                "\t\t\t<string>com.apple.webClip.managed</string>\n" +
                "\t\t\t<key>PayloadUUID</key>\n" +
                "\t\t\t<string>6CE13D92-AD76-48F2-9813-41369A401CA0</string>\n" +
                "\t\t\t<key>PayloadVersion</key>\n" +
                "\t\t\t<integer>1</integer>\n" +
                "\t\t\t<key>URL</key>\n" +
                "\t\t\t<!-- 跳转URL，需要修改 -->\n" +
                "\t\t\t<string>https://www.baidu.com</string>\n" +
                "\t\t</dict>\n" +
                "\t</array>\n" +
                "\t<key>PayloadDescription</key>\n" +
                "\t<string>该配置文件帮助用户进行APP授权安装！This configuration file helps users with APP license installation!</string>\n" +
                "\t<key>PayloadDisplayName</key>\n" +
                "\t<!-- 描述文件的显示名称，需要修改 -->\n" +
                "\t<string>百度 -- [点击安装]</string>\n" +
                "\t<key>PayloadIdentifier</key>\n" +
                "\t<!-- 标识符 每个平台不能相同，需要修改 -->\n" +
                "\t<string>abc.com</string>\n" +
                "\t<key>PayloadOrganization</key>\n" +
                "\t<string>授权安装进入下一步</string>\n" +
                "\t<key>PayloadRemovalDisallowed</key>\n" +
                "\t<false/>\n" +
                "\t<key>PayloadType</key>\n" +
                "\t<string>Configuration</string>\n" +
                "\t<key>PayloadUUID</key>\n" +
                "\t<string>BE2A2BFE-4489-457B-82DB-474D1BEDF0E5</string>\n" +
                "\t<key>PayloadVersion</key>\n" +
                "\t<integer>1</integer>\n" +
                "</dict>\n" +
                "</plist>\n";

        // 替换第三行的新内容
        String newContent = "这是新的第三行内容";

        // 调用替换方法
        String result = replaceSpecificLine(originalString, 13, newContent);



        // 打印结果
        System.out.println(result);
    }

    // 替换指定行的内容
    private static String replaceSpecificLine(String originalString, int targetLineNumber, String newContent) {
        // 将原始字符串拆分成行
        String[] lines = originalString.split("\n");

        // 检查目标行是否在有效范围内
        if (targetLineNumber > 0 && targetLineNumber <= lines.length) {
            // 替换目标行的内容
            lines[targetLineNumber - 1] = newContent;
        } else {
            System.out.println("目标行超出范围");
        }

        // 使用StringBuilder重新构建字符串
        StringBuilder result = new StringBuilder();
        for (String line : lines) {
            result.append(line).append("\n");
        }

        // 删除最后一个多余的换行符
        result.deleteCharAt(result.length() - 1);

        return result.toString();
    }

}
