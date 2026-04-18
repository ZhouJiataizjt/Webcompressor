/*
 * Reference Huffman coding
 *
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-huffman-coding
 */

package compressor.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A binary tree that represents a mapping between symbols and binary strings.
 * Immutable.
 */
public final class HuffmanCodeTree {

    public final HuffmanInternalNode root;
    private final List<int[]> codes;

    public HuffmanCodeTree(HuffmanInternalNode root, int symbolLimit) {
        this.root = Objects.requireNonNull(root);
        if (symbolLimit < 2)
            throw new IllegalArgumentException("At least 2 symbols needed");

        codes = new ArrayList<>();
        for (int i = 0; i < symbolLimit; i++)
            codes.add(null);
        buildCodeList(root, new ArrayList<Integer>());
    }

    private void buildCodeList(HuffmanNode node, List<Integer> prefix) {
        if (node instanceof HuffmanInternalNode internalNode) {
            prefix.add(0);
            buildCodeList(internalNode.leftChild(), prefix);
            prefix.remove(prefix.size() - 1);

            prefix.add(1);
            buildCodeList(internalNode.rightChild(), prefix);
            prefix.remove(prefix.size() - 1);
        } else if (node instanceof HuffmanLeaf leaf) {
            int sym = leaf.symbol();
            if (sym >= codes.size())
                throw new IllegalArgumentException("Symbol exceeds symbol limit");
            if (codes.get(sym) != null)
                throw new IllegalArgumentException("Symbol has more than one code");
            int[] codeArray = new int[prefix.size()];
            for (int i = 0; i < prefix.size(); i++)
                codeArray[i] = prefix.get(i);
            codes.set(sym, codeArray);
        } else {
            throw new AssertionError("Illegal node type");
        }
    }

    /**
     * Returns the Huffman code for the specified symbol, which is a list of 0s and 1s.
     */
    public int[] getCode(int symbol) {
        if (symbol < 0)
            throw new IllegalArgumentException("Illegal symbol");
        if (codes.get(symbol) == null)
            throw new IllegalArgumentException("No code for given symbol");
        return codes.get(symbol);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString("", root, sb);
        return sb.toString();
    }

    private static void toString(String prefix, HuffmanNode node, StringBuilder sb) {
        if (node instanceof HuffmanInternalNode internalNode) {
            toString(prefix + "0", internalNode.leftChild(), sb);
            toString(prefix + "1", internalNode.rightChild(), sb);
        } else if (node instanceof HuffmanLeaf leaf) {
            sb.append(String.format("Code %s: Symbol %d%n", prefix, leaf.symbol()));
        } else {
            throw new AssertionError("Illegal node type");
        }
    }
}
