/*
 * Reference Huffman coding
 *
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-huffman-coding
 */

package compressor.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A canonical Huffman code, which only describes the code length of
 * each symbol. Immutable. Code length 0 means no code for the symbol.
 * The binary codes for each symbol can be reconstructed from the length information.
 * In this implementation, lexicographically lower binary codes are assigned to symbols
 * with lower code lengths, breaking ties by lower symbol values.
 */
public final class CanonicalCode {

    private final int[] codeLengths;

    public CanonicalCode(int[] codeLens) {
        Objects.requireNonNull(codeLens);
        if (codeLens.length < 2)
            throw new IllegalArgumentException("At least 2 symbols needed");
        for (int cl : codeLens) {
            if (cl < 0)
                throw new IllegalArgumentException("Illegal code length");
        }

        codeLengths = codeLens.clone();
        Arrays.sort(codeLengths);
        int currentLevel = codeLengths[codeLengths.length - 1];
        int numNodesAtLevel = 0;
        for (int i = codeLengths.length - 1; i >= 0 && codeLengths[i] > 0; i--) {
            int cl = codeLengths[i];
            while (cl < currentLevel) {
                if (numNodesAtLevel % 2 != 0)
                    throw new IllegalArgumentException("Under-full Huffman code tree");
                numNodesAtLevel /= 2;
                currentLevel--;
            }
            numNodesAtLevel++;
        }
        while (currentLevel > 0) {
            if (numNodesAtLevel % 2 != 0)
                throw new IllegalArgumentException("Under-full Huffman code tree");
            numNodesAtLevel /= 2;
            currentLevel--;
        }
        if (numNodesAtLevel < 1)
            throw new IllegalArgumentException("Under-full Huffman code tree");
        if (numNodesAtLevel > 1)
            throw new IllegalArgumentException("Over-full Huffman code tree");

        System.arraycopy(codeLens, 0, codeLengths, 0, codeLens.length);
    }

    public CanonicalCode(HuffmanCodeTree tree, int symbolLimit) {
        Objects.requireNonNull(tree);
        if (symbolLimit < 2)
            throw new IllegalArgumentException("At least 2 symbols needed");
        codeLengths = new int[symbolLimit];
        buildCodeLengths(tree.root, 0);
    }

    private void buildCodeLengths(HuffmanNode node, int depth) {
        if (node instanceof HuffmanInternalNode internalNode) {
            buildCodeLengths(internalNode.leftChild(), depth + 1);
            buildCodeLengths(internalNode.rightChild(), depth + 1);
        } else if (node instanceof HuffmanLeaf leaf) {
            int sym = leaf.symbol();
            if (sym >= codeLengths.length)
                throw new IllegalArgumentException("Symbol exceeds symbol limit");
            if (codeLengths[sym] != 0)
                throw new AssertionError("Symbol has more than one code");
            codeLengths[sym] = depth;
        } else {
            throw new AssertionError("Illegal node type");
        }
    }

    public int getSymbolLimit() {
        return codeLengths.length;
    }

    public int getCodeLength(int symbol) {
        if (symbol < 0 || symbol >= codeLengths.length)
            throw new IllegalArgumentException("Symbol out of range");
        return codeLengths[symbol];
    }

    public HuffmanCodeTree toCodeTree() {
        List<HuffmanNode> nodes = new ArrayList<>();
        for (int i = max(codeLengths); i >= 0; i--) {
            if (nodes.size() % 2 != 0)
                throw new AssertionError("Violation of canonical code invariants");
            List<HuffmanNode> newNodes = new ArrayList<>();

            if (i > 0) {
                for (int j = 0; j < codeLengths.length; j++) {
                    if (codeLengths[j] == i)
                        newNodes.add(new HuffmanLeaf(j));
                }
            }

            for (int j = 0; j < nodes.size(); j += 2)
                newNodes.add(new HuffmanInternalNode(nodes.get(j), nodes.get(j + 1)));
            nodes = newNodes;
        }

        if (nodes.size() != 1)
            throw new AssertionError("Violation of canonical code invariants");
        return new HuffmanCodeTree((HuffmanInternalNode) nodes.get(0), codeLengths.length);
    }

    private static int max(int[] array) {
        int result = array[0];
        for (int x : array)
            result = Math.max(result, x);
        return result;
    }
}
