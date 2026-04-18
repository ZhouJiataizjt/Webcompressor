/*
 * Reference Huffman coding
 *
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-huffman-coding
 */

package compressor.utils;

/**
 * A leaf node in a Huffman code tree. It holds a symbol value.
 */
public final class HuffmanLeaf implements HuffmanNode {

    private final int symbol;

    public HuffmanLeaf(int symbol) {
        if (symbol < 0)
            throw new IllegalArgumentException("Symbol value must be non-negative");
        this.symbol = symbol;
    }

    public int symbol() {
        return symbol;
    }
}
