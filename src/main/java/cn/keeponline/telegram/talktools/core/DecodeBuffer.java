package cn.keeponline.telegram.talktools.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * 解码缓冲区，用于数据包解码
 */
public class DecodeBuffer {
    private final ByteBuffer buffer;
    private int pos = 0;

    public DecodeBuffer(byte[] data) {
        this.buffer = ByteBuffer.wrap(data);
        this.buffer.order(ByteOrder.BIG_ENDIAN);
    }

    public int readUint8() {
        return buffer.get(pos++) & 0xFF;
    }

    public int readUint16() {
        int b0 = buffer.get(pos++) & 0xFF;
        int b1 = buffer.get(pos++) & 0xFF;
        return (b0 << 8) | b1;
    }

    public int readInt32() {
        int result = buffer.getInt(pos);
        pos += 4;
        return result;
    }

    public long readInt64() {
        long result = buffer.getLong(pos);
        pos += 8;
        return result;
    }

    public byte[] readBytes(int length) {
        byte[] result = new byte[length];
        buffer.position(pos);
        buffer.get(result);
        pos += length;
        return result;
    }

    public String readString() {
        int length = readUint16();
        byte[] val = readBytes(length);
        return new String(val, StandardCharsets.UTF_8);
    }

    public byte[] readRemaining() {
        int remaining = buffer.remaining() - (pos - buffer.position());
        if (remaining <= 0) {
            return new byte[0];
        }
        return readBytes(remaining);
    }

    public int readVariableLength() {
        int multiplier = 1;
        int value = 0;
        while (true) {
            int encodedByte = readUint8();
            value += (encodedByte & 127) * multiplier;
            if ((encodedByte & 128) == 0) {
                break;
            }
            multiplier *= 128;
            if (multiplier > 128 * 128 * 128) {
                throw new RuntimeException("Malformed Remaining Length");
            }
        }
        return value;
    }
}

