package compressor.algorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BrotliCompressorTest {

    private BrotliCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = new BrotliCompressor();
    }

    @Test
    void testCompressAndDecompressSimpleHTML() throws IOException {
        String html = "<html><head><title>Test</title></head><body><div>Hello World</div></body></html>";
        byte[] original = html.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        System.out.println("HTML - Original: " + original.length + " bytes, Compressed: " + compressed.length + " bytes");
        
        assertTrue(compressed.length < original.length, "HTML should be compressible");

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        assertEquals(html, result, "HTML should be losslessly decompressed");
    }

    @Test
    void testCompressAndDecompressComplexHTML() throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Complex Page</title>
                <link rel="stylesheet" href="style.css">
            </head>
            <body>
                <header><nav><main>
                    <div class="container">
                        <h1>Welcome</h1>
                        <p>Paragraph text here</p>
                        <a href="https://example.com">Link</a>
                    </div>
                </main></nav></header>
            </body>
            </html>
            """;
        byte[] original = html.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        System.out.println("Complex HTML - Original: " + original.length + " bytes, Compressed: " + compressed.length + " bytes");
        System.out.println("Ratio: " + String.format("%.2f%%", (1 - (double) compressed.length / original.length) * 100));
        System.out.println(compressor.getStats());

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        assertEquals(html, result, "Complex HTML should be losslessly decompressed");
    }

    @Test
    void testCompressAndDecompressCSS() throws IOException {
        String css = """
            body { font-family: Arial; color: #333; }
            .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
            #header { background: #f5f5f5; height: 80px; }
            """;
        byte[] original = css.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        System.out.println("CSS - Original: " + original.length + " bytes, Compressed: " + compressed.length + " bytes");

        byte[] decompressed = compressor.decompress(compressed);

        assertTrue(Arrays.equals(original, decompressed), "CSS should be losslessly decompressed");
    }

    @Test
    void testCompressAndDecompressJS() throws IOException {
        String js = """
            function init() {
                var element = document.getElementById('app');
                if (element) {
                    element.innerHTML = '<div>Loaded</div>';
                }
                return true;
            }
            window.onload = init;
            """;
        byte[] original = js.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        System.out.println("JS - Original: " + original.length + " bytes, Compressed: " + compressed.length + " bytes");
        System.out.println(compressor.getStats());

        byte[] decompressed = compressor.decompress(compressed);

        assertTrue(Arrays.equals(original, decompressed), "JS should be losslessly decompressed");
    }

    @Test
    void testEmptyData() throws IOException {
        byte[] original = new byte[0];
        byte[] compressed = compressor.compress(original);

        assertEquals(0, compressed.length, "Empty data should compress to empty");
    }

    @Test
    void testInvalidFormat() {
        byte[] invalidData = "NOTBROT".getBytes(StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> compressor.decompress(invalidData));
    }

    @Test
    void testSingleSymbolData() throws IOException {
        byte[] original = new byte[1000];
        Arrays.fill(original, (byte) 'A');

        byte[] compressed = compressor.compress(original);
        byte[] decompressed = compressor.decompress(compressed);

        assertTrue(Arrays.equals(original, decompressed), "Single symbol data should be losslessly decompressed");
    }

    @Test
    void testMultipleOccurrences() throws IOException {
        String html = "<html><body><div></div><div></div><div></div></body></html>";
        byte[] original = html.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        System.out.println("Multiple div tags - Original: " + original.length + ", Compressed: " + compressed.length);

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        assertEquals(html, result, "Multiple occurrences should all be restored");
    }

    @Test
    void testChineseContent() throws IOException {
        String chinese = "你好，世界！这是一个测试网页，包含中文内容。";
        byte[] original = chinese.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        System.out.println("Chinese - Original: " + original.length + " bytes, Compressed: " + compressed.length + " bytes");

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        assertEquals(chinese, result, "Chinese content should be losslessly decompressed");
    }

    @Test
    void testCompareWithGZip() throws IOException {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8">
                <title>Test Page</title>
            </head>
            <body>
                <div class="container">
                    <h1>Hello World</h1>
                    <p>This is a test paragraph.</p>
                </div>
            </body>
            </html>
            """;
        byte[] original = html.getBytes(StandardCharsets.UTF_8);

        // Brotli
        BrotliCompressor brotli = new BrotliCompressor();
        byte[] brotliCompressed = brotli.compress(original);
        
        // GZIP for comparison
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.util.zip.GZIPOutputStream gzipOut = new java.util.zip.GZIPOutputStream(baos);
        gzipOut.write(original);
        gzipOut.close();
        byte[] gzipCompressed = baos.toByteArray();

        System.out.println("Comparison:");
        System.out.println("Original: " + original.length);
        System.out.println("Brotli: " + brotliCompressed.length + " (" + 
            String.format("%.2f%%", (1 - (double)brotliCompressed.length / original.length) * 100) + ")");
        System.out.println("GZIP: " + gzipCompressed.length + " (" + 
            String.format("%.2f%%", (1 - (double)gzipCompressed.length / original.length) * 100) + ")");

        // Brotli should typically be better than GZIP
        assertTrue(brotliCompressed.length <= gzipCompressed.length,
            "Brotli should be at least as good as GZIP");
    }
}
