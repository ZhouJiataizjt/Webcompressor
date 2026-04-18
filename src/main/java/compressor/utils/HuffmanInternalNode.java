/*
 * Reference Huffman coding
 *
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-huffman-coding
 */

package compressor.utils;

import java.util.Objects;

/**
 * An internal node in a Huffman code tree. It has two child nodes.
 */
public final class HuffmanInternalNode implements HuffmanNode {

    private final HuffmanNode leftChild;
    private final HuffmanNode rightChild;

    public HuffmanInternalNode(HuffmanNode leftChild, HuffmanNode rightChild) {
        this.leftChild = Objects.requireNonNull(leftChild);
        this.rightChild = Objects.requireNonNull(rightChild);
    }

    public HuffmanNode leftChild() {
        return leftChild;
    }

    public HuffmanNode rightChild() {
        return rightChild;
    }
}
