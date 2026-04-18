/*
 * Reference Huffman coding
 *
 * Copyright (c) Project Nayuki
 * MIT License. See readme file.
 * https://www.nayuki.io/page/reference-huffman-coding
 */

package compressor.utils;

import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * A table of symbol frequencies. Mutable and not thread-safe.
 */
public final class FrequencyTable {

    private final int[] frequencies;

    public FrequencyTable(int[] freqs) {
        Objects.requireNonNull(freqs);
        if (freqs.length < 2)
            throw new IllegalArgumentException("At least 2 symbols needed");
        frequencies = freqs.clone();
        for (int x : frequencies) {
            if (x < 0)
                throw new IllegalArgumentException("Negative frequency");
        }
    }

    public int getSymbolLimit() {
        return frequencies.length;
    }

    public int get(int symbol) {
        checkSymbol(symbol);
        return frequencies[symbol];
    }

    public void set(int symbol, int freq) {
        checkSymbol(symbol);
        if (freq < 0)
            throw new IllegalArgumentException("Negative frequency");
        frequencies[symbol] = freq;
    }

    public void increment(int symbol) {
        checkSymbol(symbol);
        if (frequencies[symbol] == Integer.MAX_VALUE)
            throw new IllegalStateException("Maximum frequency reached");
        frequencies[symbol]++;
    }

    private void checkSymbol(int symbol) {
        if (symbol < 0 || symbol >= frequencies.length)
            throw new IllegalArgumentException("Symbol out of range");
    }

    /**
     * Returns a code tree that is optimal for the symbol frequencies in this table.
     */
    public HuffmanCodeTree buildCodeTree() {
        Queue<HuffmanNodeWithFreq> pqueue = new PriorityQueue<>();

        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0)
                pqueue.add(new HuffmanNodeWithFreq(new HuffmanLeaf(i), i, frequencies[i]));
        }

        for (int i = 0; i < frequencies.length && pqueue.size() < 2; i++) {
            if (frequencies[i] == 0)
                pqueue.add(new HuffmanNodeWithFreq(new HuffmanLeaf(i), i, 0));
        }
        if (pqueue.size() < 2)
            throw new AssertionError();

        while (pqueue.size() > 1) {
            HuffmanNodeWithFreq x = pqueue.remove();
            HuffmanNodeWithFreq y = pqueue.remove();
            pqueue.add(new HuffmanNodeWithFreq(
                    new HuffmanInternalNode(x.node(), y.node()),
                    Math.min(x.lowestSymbol(), y.lowestSymbol()),
                    x.frequency() + y.frequency()));
        }

        return new HuffmanCodeTree((HuffmanInternalNode) pqueue.remove().node(), frequencies.length);
    }

    private static class HuffmanNodeWithFreq implements Comparable<HuffmanNodeWithFreq> {
        private final HuffmanNode node;
        private final int lowestSymbol;
        private final long frequency;

        public HuffmanNodeWithFreq(HuffmanNode node, int lowestSymbol, long frequency) {
            this.node = node;
            this.lowestSymbol = lowestSymbol;
            this.frequency = frequency;
        }

        public HuffmanNode node() { return node; }
        public int lowestSymbol() { return lowestSymbol; }
        public long frequency() { return frequency; }

        public int compareTo(HuffmanNodeWithFreq other) {
            if (frequency != other.frequency)
                return Long.compare(frequency, other.frequency);
            else
                return Integer.compare(lowestSymbol, other.lowestSymbol);
        }
    }
}
