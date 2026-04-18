package compressor.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * A stream where bits can be written to. Bits are written LSB first (little-endian),
 * so that the decoder can read them MSB first (big-endian) from the byte stream.
 * Mutable and not thread-safe.
 */
public final class BitOutputStream implements AutoCloseable {

    private final OutputStream output;
    private final ByteArrayOutputStream baosInternal;
    private final boolean ownsOutput;
    private int currentByte;
    private int numBitsFilled;

    public BitOutputStream(OutputStream out) {
        output = Objects.requireNonNull(out);
        baosInternal = null;
        ownsOutput = false;
        currentByte = 0;
        numBitsFilled = 0;
    }

    public BitOutputStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        output = baos;
        baosInternal = baos;
        ownsOutput = true;
        currentByte = 0;
        numBitsFilled = 0;
    }

    /**
     * Writes a bit (0 or 1). Bits are accumulated LSB first.
     * When 8 bits are accumulated, they are flushed to the underlying stream.
     */
    public void write(int b) throws IOException {
        if (b != 0 && b != 1)
            throw new IllegalArgumentException("Argument must be 0 or 1");
        currentByte |= (b << numBitsFilled);
        numBitsFilled++;
        if (numBitsFilled == 8) {
            output.write(currentByte);
            currentByte = 0;
            numBitsFilled = 0;
        }
    }

    public void writeBit(int bit) throws IOException {
        write(bit & 1);
    }

    public void writeBits(int value, int numBits) throws IOException {
        for (int i = 0; i < numBits; i++) {
            int bit = (value >>> i) & 1;
            write(bit);
        }
    }

    public void writeByte(int b) throws IOException {
        for (int i = 0; i < 8; i++) {
            write((b >>> i) & 1);
        }
    }

    public void writeBytes(byte[] data) throws IOException {
        for (byte b : data) {
            writeByte(b & 0xFF);
        }
    }

    public void alignToByte() throws IOException {
        while (numBitsFilled != 0)
            write(0);
    }

    public void flush() throws IOException {
        alignToByte();
        output.flush();
    }

    public void close() throws IOException {
        flush();
        if (ownsOutput) {
            output.close();
        }
    }

    public byte[] toByteArray() {
        if (baosInternal == null) {
            throw new IllegalStateException("toByteArray() only available for BitOutputStream() constructor");
        }
        return baosInternal.toByteArray();
    }

    public int size() {
        if (baosInternal == null) {
            throw new IllegalStateException("size() only available for BitOutputStream() constructor");
        }
        return baosInternal.size();
    }
}
