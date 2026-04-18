package compressor.test;

import compressor.utils.*;
import java.io.*;
import java.util.Arrays;

public class HuffmanTest {
    public static void main(String[] args) throws Exception {
        // Test 1: Simple byte sequence
        System.out.println("=== Test 1: Simple byte sequence ===");
        byte[] testData = new byte[]{65, 66, 65, 67, 65};
        System.out.println("Original: " + Arrays.toString(testData));

        byte[] compressed = compressBytes(testData);
        System.out.println("Compressed size: " + compressed.length);

        byte[] decompressed = decompressBytes(compressed);
        System.out.println("Decompressed: " + Arrays.toString(decompressed));

        if (Arrays.equals(testData, decompressed)) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }

        // Test 2: All byte values
        System.out.println("\n=== Test 2: All 256 byte values ===");
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) allBytes[i] = (byte) i;
        byte[] allDecompressed = decompressBytes(compressBytes(allBytes));
        if (Arrays.equals(allBytes, allDecompressed)) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }

        // Test 3: Single repeated byte
        System.out.println("\n=== Test 3: Single repeated byte ===");
        byte[] single = new byte[100];
        Arrays.fill(single, (byte) 'A');
        byte[] singleDec = decompressBytes(compressBytes(single));
        if (Arrays.equals(single, singleDec)) {
            System.out.println("PASS");
        } else {
            System.out.println("FAIL");
        }
    }

    private static byte[] compressBytes(byte[] data) throws Exception {
        int SYMBOL_LIMIT = 257;

        int[] frequencies = new int[SYMBOL_LIMIT];
        for (byte b : data) {
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
