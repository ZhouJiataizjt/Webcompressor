package compressor.algorithms;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebDictCompressor 测试
 */
class WebDictCompressorTest {

    private WebDictCompressor compressor;

    @BeforeEach
    void setUp() {
        compressor = new WebDictCompressor();
    }

    @Test
    void testCompressAndDecompressSimpleHTML() throws IOException {
        String html = "<html><head><title>Test</title></head><body><div>Hello World</div></body></html>";
        byte[] original = html.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        assertTrue(compressed.length <= original.length, "压缩后应该不大于原始大小");

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        assertEquals(html, result, "HTML 应该无损解压");
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

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        assertEquals(html, result, "Complex HTML 应该无损解压");
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

        assertTrue(Arrays.equals(original, decompressed), "CSS 应该无损解压");
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

        byte[] decompressed = compressor.decompress(compressed);

        assertTrue(Arrays.equals(original, decompressed), "JS 应该无损解压");
    }

    @Test
    void testMultipleOccurrences() throws IOException {
        String html = "<html><body><div></div><div></div><div></div></body></html>";
        byte[] original = html.getBytes(StandardCharsets.UTF_8);

        byte[] compressed = compressor.compress(original);
        System.out.println("Multiple div tags - Original: " + original.length + ", Compressed: " + compressed.length);

        byte[] decompressed = compressor.decompress(compressed);
        String result = new String(decompressed, StandardCharsets.UTF_8);

        assertEquals(html, result, "多个相同标签应该全部正确还原");
    }

    @Test
    void testEmptyData() throws IOException {
        byte[] original = new byte[0];
        byte[] compressed = compressor.compress(original);

        assertEquals(0, compressed.length, "空数据应该压缩为空");
    }

    @Test
    void testInvalidFormat() {
        byte[] invalidData = "NOTWDCT".getBytes(StandardCharsets.UTF_8);

        assertThrows(IOException.class, () -> compressor.decompress(invalidData));
    }

    @Test
    void testSingleSymbolData() throws IOException {
        byte[] original = new byte[1000];
        Arrays.fill(original, (byte) 'A');

        byte[] compressed = compressor.compress(original);
        byte[] decompressed = compressor.decompress(compressed);

        assertTrue(Arrays.equals(original, decompressed), "单符号数据应该无损解压");
    }

    @Test
    void testDictionarySize() {
        System.out.println("字典大小: " + WebDictionary.size());
        assertTrue(WebDictionary.size() <= 256, "字典大小不应超过 256");
        assertTrue(WebDictionary.size() > 100, "字典大小应该大于 100");
    }

    @Test
    void testCompareWithBrotli() throws IOException {
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

        // WebDict
        WebDictCompressor webdict = new WebDictCompressor();
        byte[] webdictCompressed = webdict.compress(original);
        
        // Brotli for comparison
        BrotliCompressor brotli = new BrotliCompressor();
        byte[] brotliCompressed = brotli.compress(original);

        System.out.println("Comparison:");
        System.out.println("Original: " + original.length);
        System.out.println("WebDict: " + webdictCompressed.length + " (" + 
            String.format("%.2f%%", (1 - (double)webdictCompressed.length / original.length) * 100) + ")");
        System.out.println("Brotli: " + brotliCompressed.length + " (" + 
            String.format("%.2f%%", (1 - (double)brotliCompressed.length / original.length) * 100) + ")");

        // 验证两者都能正确解压
        byte[] webdictDecomp = webdict.decompress(webdictCompressed);
        byte[] brotliDecomp = brotli.decompress(brotliCompressed);
        
        assertTrue(Arrays.equals(original, webdictDecomp), "WebDict 应该无损解压");
        assertTrue(Arrays.equals(original, brotliDecomp), "Brotli 应该无损解压");
    }
}
