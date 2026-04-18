package compressor.algorithms;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class QuickTest {
    public static void main(String[] args) throws IOException {
        System.out.println("Testing WebDictCompressor...");

        WebDictCompressor compressor = new WebDictCompressor();

        // Test 1: Simple HTML
        String html = "<html><head><title>Test</title></head><body><div>Hello World</div></body></html>";
        byte[] original = html.getBytes(StandardCharsets.UTF_8);

        System.out.println("Original: " + original.length + " bytes");
        byte[] compressed = compressor.compress(original);
        System.out.println("Compressed: " + compressed.length + " bytes");

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        if (html.equals(result)) {
            System.out.println("TEST PASSED: Simple HTML");
        } else {
            System.out.println("TEST FAILED: Simple HTML");
            System.out.println("Expected: " + html);
            System.out.println("Got: " + result);
        }

        // Test 2: Complex HTML
        String complexHtml = "<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>Complex Page</title></head><body><header><nav><main><div class=\"container\"><h1>Welcome</h1></div></main></nav></header></body></html>";
        byte[] complexOriginal = complexHtml.getBytes(StandardCharsets.UTF_8);

        WebDictCompressor compressor2 = new WebDictCompressor();
        byte[] complexCompressed = compressor2.compress(complexOriginal);
        byte[] complexDecompressed = compressor2.decompress(complexCompressed);
        String complexResult = new String(complexDecompressed, StandardCharsets.UTF_8);

        if (complexHtml.equals(complexResult)) {
            System.out.println("TEST PASSED: Complex HTML");
        } else {
            System.out.println("TEST FAILED: Complex HTML");
            System.out.println("Expected: " + complexHtml);
            System.out.println("Got: " + complexResult);
        }

        // Test 3: Single symbol
        byte[] singleSymbol = new byte[100];
        for (int i = 0; i < singleSymbol.length; i++) singleSymbol[i] = (byte) 'A';

        WebDictCompressor compressor3 = new WebDictCompressor();
        byte[] singleCompressed = compressor3.compress(singleSymbol);
        byte[] singleDecompressed = compressor3.decompress(singleCompressed);

        boolean allMatch = true;
        for (int i = 0; i < singleSymbol.length; i++) {
            if (singleSymbol[i] != singleDecompressed[i]) {
                allMatch = false;
                break;
            }
        }
        if (allMatch) {
            System.out.println("TEST PASSED: Single symbol");
        } else {
            System.out.println("TEST FAILED: Single symbol");
        }

        System.out.println("All quick tests complete!");
    }
}
