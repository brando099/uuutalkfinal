package cn.keeponline.telegram.talktools.core;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 字节缓冲区，用于数据包编码
 * 等价于 JS 里的 ByteBuffer
 */
public class PacketByteBuffer {
    private final List<Byte> buffer;

    public PacketByteBuffer() {
        this.buffer = new ArrayList<>();
    }

    public PacketByteBuffer(int capacity) {
        this.buffer = new ArrayList<>(capacity);
    }

    public void writeUint8(int value) {
        buffer.add((byte) (value & 0xFF));
    }

    public void writeInt16(int value) {
        buffer.add((byte) ((value >> 8) & 0xFF));
        buffer.add((byte) (value & 0xFF));
    }

    public void writeInt32(int value) {
        buffer.add((byte) ((value >> 24) & 0xFF));
        buffer.add((byte) ((value >> 16) & 0xFF));
        buffer.add((byte) ((value >> 8) & 0xFF));
        buffer.add((byte) (value & 0xFF));
    }

    public void writeInt64(long value) {
        buffer.add((byte) ((value >> 56) & 0xFF));
        buffer.add((byte) ((value >> 48) & 0xFF));
        buffer.add((byte) ((value >> 40) & 0xFF));
        buffer.add((byte) ((value >> 32) & 0xFF));
        buffer.add((byte) ((value >> 24) & 0xFF));
        buffer.add((byte) ((value >> 16) & 0xFF));
        buffer.add((byte) ((value >> 8) & 0xFF));
        buffer.add((byte) (value & 0xFF));
    }

    public void writeBytes(byte[] data) {
        for (byte b : data) {
            buffer.add(b);
        }
    }

    public void writeString(String s) {
        if (s == null || s.isEmpty()) {
            writeInt16(0);
            return;
        }
        byte[] bs = s.getBytes(StandardCharsets.UTF_8);
        writeInt16(bs.length);
        writeBytes(bs);
    }

    /**
     * MQTT 风格的可变长度编码
     */
    public void writeVariableLength(int value) {
        while (true) {
            int digit = value & 0x7F;
            value >>= 7;
            if (value > 0) {
                digit |= 0x80;
            }
            writeUint8(digit);
            if (value == 0) {
                break;
            }
        }
    }

    public byte[] toBytes() {
        byte[] result = new byte[buffer.size()];
        for (int i = 0; i < buffer.size(); i++) {
            result[i] = buffer.get(i);
        }
        return result;
    }
}
