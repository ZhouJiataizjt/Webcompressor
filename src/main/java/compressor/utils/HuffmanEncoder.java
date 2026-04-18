/*
 * Reference Huffman coding
 *
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-huffman-coding
 */

package compressor.utils;

import java.io.IOException;
import java.util.Objects;

/**
 * Encodes symbols and writes to a Huffman-coded bit stream. Not thread-safe.
 */
public final class HuffmanEncoder {

    private final BitOutputStream output;
    public HuffmanCodeTree codeTree;

    public HuffmanEncoder(BitOutputStream out) {
        output = Objects.requireNonNull(out);
    }

    public void write(int symbol) throws IOException {
        if (codeTree == null)
            throw new IllegalStateException("Code tree is null");
        int[] bits = codeTree.getCode(symbol);
        for (int b : bits)
            output.write(b);
    }

    public void writeBit(int bit) throws IOException {
        output.write(bit);
    }
}
