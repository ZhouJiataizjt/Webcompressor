package compressor.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * A stream of bits that can be read. Because they come from an underlying byte stream,
 * the total number of bits is always a multiple of 8.
 *
 * IMPORTANT: Bits are read LSB first to match the BitOutputStream's write order.
 * When writing, bits are accumulated LSB first. When reading, we extract from LSB first
 * so that [1, 0] written as byte 0b01 becomes read back as [1, 0].
 *
 * Mutable and not thread-safe.
 */
public final class BitInputStream implements AutoCloseable {

    private final InputStream input;
    private int currentByte;
    private int numBitsRemaining;

    public BitInputStream(InputStream in) {
        input = Objects.requireNonNull(in);
        currentByte = 0;
        numBitsRemaining = 0;
    }

    public BitInputStream(byte[] data) {
        this(new java.io.ByteArrayInputStream(data));
    }

    /**
     * Reads a bit (0 or 1). Bits are extracted LSB first from the byte stream.
     */
    public int readBit() throws IOException {
        if (numBitsRemaining == 0) {
            currentByte = input.read();
            if (currentByte == -1)
                return -1;
            numBitsRemaining = 8;
        }
        int bit = currentByte & 1;
        currentByte >>>= 1;
        numBitsRemaining--;
        return bit;
    }

    /**
     * Reads a bit, throws EOFException if end of stream.
     */
    public int read() throws IOException {
        if (numBitsRemaining == 0) {
            currentByte = input.read();
            if (currentByte == -1)
                return -1;
            numBitsRemaining = 8;
        }
        int bit = currentByte & 1;
        currentByte >>>= 1;
        numBitsRemaining--;
        return bit;
    }

    /**
     * Reads a bit, throws EOFException if end of stream.
     */
    public int readNoEof() throws IOException {
        int result = read();
        if (result != -1)
            return result;
        else
            throw new EOFException("End of stream reached");
    }

    public int readBits(int numBits) throws IOException {
        int result = 0;
        for (int i = 0; i < numBits; i++) {
            int bit = read();
            if (bit == -1)
                return -1;
            result |= (bit << i);
        }
        return result;
    }

    public int readByte() throws IOException {
        return readBits(8);
    }

    public int readByteNoEof() throws IOException {
        int b = input.read();
        if (b == -1)
            throw new EOFException("End of stream");
        return b;
    }

    public boolean hasRemaining() throws IOException {
        if (numBitsRemaining > 0)
            return true;
        return input.available() > 0;
    }

    public int available() throws IOException {
        int bitsAvailable = numBitsRemaining + input.available() * 8;
        return bitsAvailable / 8;
    }

    public void skipToByteBoundary() {
        numBitsRemaining = 0;
    }

    @Override
    public void close() throws IOException {
        input.close();
        currentByte = -1;
        numBitsRemaining = 0;
    }
}
