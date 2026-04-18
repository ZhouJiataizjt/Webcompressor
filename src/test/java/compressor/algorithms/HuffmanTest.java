package compressor.algorithms;

import compressor.utils.*;
import java.io.*;

public class HuffmanTest {
    public static void main(String[] args) throws Exception {
        // Test 1: Simple Huffman codec
        System.out.println("=== Test 1: Simple byte sequence ===");
        byte[] testData = new byte[]{65, 66, 65, 67, 65};
        System.out.println("Original: " + bytesToString(testData));

        // Compress
        byte[] compressed = compressBytes(testData);
        System.out.println("Compressed size: " + compressed.length + " bytes");

        // Decompress
        byte[] decompressed = decompressBytes(compressed);
        System.out.println("Decompressed: " + bytesToString(decompressed));

        if (java.util.Arrays.equals(testData, decompressed)) {
            System.out.println("PASS: Simple sequence");
        } else {
            System.out.println("FAIL: Simple sequence");
        }

        // Test 2: All byte values
        System.out.println("\n=== Test 2: All byte values ===");
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) allBytes[i] = (byte) i;
        byte[] allCompressed = compressBytes(allBytes);
        byte[] allDecompressed = decompressBytes(allCompressed);
        if (java.util.Arrays.equals(allBytes, allDecompressed)) {
            System.out.println("PASS: All byte values");
        } else {
            System.out.println("FAIL: All byte values");
        }

        // Test 3: Single repeated byte
        System.out.println("\n=== Test 3: Single repeated byte ===");
        byte[] single = new byte[100];
        java.util.Arrays.fill(single, (byte) 'A');
        byte[] singleCompressed = compressBytes(single);
        byte[] singleDecompressed = decompressBytes(singleCompressed);
        if (java.util.Arrays.equals(single, singleDecompressed)) {
            System.out.println("PASS: Single repeated byte");
        } else {
            System.out.println("FAIL: Single repeated byte");
        }
    }

    private static String bytesToString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            sb.append(b).append(" ");
        }
        return sb.toString().trim();
    }

    private static byte[] compressBytes(byte[] data) throws Exception {
        int SYMBOL_LIMIT = 257;

        int[] frequencies = new int[SYMBOL_LIMIT];
        for (int b : data) {
            frequencies[b & 0xFF]++;
        }
        frequencies[SYMBOL_LIMIT - 1] = 1;

        FrequencyTable freqTable = new FrequencyTable(frequencies);
        HuffmanCodeTree codeTree = freqTable.buildCodeTree();
        CanonicalCode canonCode = new CanonicalCode(codeTree, SYMBOL_LIMIT);
        HuffmanCodeTree finalTree = canonCode.toCodeTree();

        ByteArrayOutputStream headerOut = new ByteArrayOutputStream();
        for (int i = 0; i < SYMBOL_LIMIT; i++) {
            headerOut.write(canonCode.getCodeLength(i));
        }

        BitOutputStream bitOut = new BitOutputStream();
        HuffmanEncoder encoder = new HuffmanEncoder(bitOut);
        encoder.codeTree = finalTree;
        for (byte b : data) {
            encoder.write(b & 0xFF);
        }
        encoder.write(SYMBOL_LIMIT - 1);
        bitOut.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(headerOut.toByteArray());
        out.write(bitOut.toByteArray());
        return out.toByteArray();
    }

    private static byte[] decompressBytes(byte[] data) throws Exception {
        int SYMBOL_LIMIT = 257;

        ByteArrayInputStream in = new ByteArrayInputStream(data);
        int[] codeLengths = new int[SYMBOL_LIMIT];
        for (int i = 0; i < SYMBOL_LIMIT; i++) {
            codeLengths[i] = in.read();
        }

        CanonicalCode canonCode = new CanonicalCode(codeLengths);
        HuffmanCodeTree codeTree = canonCode.toCodeTree();

        byte[] bitData = new byte[data.length - SYMBOL_LIMIT];
        in.read(bitData);
        BitInputStream bitIn = new BitInputStream(bitData);
        HuffmanDecoder decoder = new HuffmanDecoder(bitIn);
        decoder.codeTree = codeTree;

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        while (true) {
            try {
                int sym = decoder.read();
                if (sym == SYMBOL_LIMIT - 1) break;
                result.write(sym);
            } catch (EOFException e) {
                break;
            }
        }
        return result.toByteArray();
    }
}
