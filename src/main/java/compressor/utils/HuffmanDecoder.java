/*
 * Reference Huffman coding
 *
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-huffman-coding
 */

package compressor.utils;

import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

/**
 * Reads from a Huffman-coded bit stream and decodes symbols. Not thread-safe.
 */
public final class HuffmanDecoder {

    private final BitInputStream input;
    public HuffmanCodeTree codeTree;

    public HuffmanDecoder(BitInputStream in) {
        input = Objects.requireNonNull(in);
    }

    /**
     * Reads from the input stream to decode the next Huffman-coded symbol.
     * @return the next symbol in the stream, which is non-negative
     * @throws EOFException if the end of stream was reached before a symbol was decoded
     */
    public int read() throws IOException {
        if (codeTree == null)
            throw new IllegalStateException("Code tree is null");

        HuffmanInternalNode currentNode = codeTree.root;
        while (true) {
            int temp = input.readNoEof();
            HuffmanNode nextNode;
            if (temp == 0)
                nextNode = currentNode.leftChild();
            else if (temp == 1)
                nextNode = currentNode.rightChild();
            else
                throw new AssertionError("Invalid value from readNoEof()");

            if (nextNode instanceof HuffmanLeaf leaf)
                return leaf.symbol();
            else if (nextNode instanceof HuffmanInternalNode internalNode)
                currentNode = internalNode;
            else
                throw new AssertionError("Illegal node type");
        }
    }

    public boolean hasAvailable() {
        try {
            return input.available() > 0;
        } catch (java.io.IOException e) {
            return false;
        }
    }
}
