package compressor.algorithms;

import compressor.core.AbstractCompressor;
import compressor.model.CompressionStats;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * Brotli 压缩实现 - 使用 JDK 内置的 Brotli 支持
 * 
 * Brotli 是 Google 开发的压缩算法，特点：
 * 1. 内置优化的静态字典（包含常见网页元素）
 * 2. 比 GZIP 有更好的压缩率
 * 3. JDK 9+ 内置支持
 * 
 * 压缩流程：
 * 1. 直接对原始数据进行 Brotli 压缩
 * 2. 使用内置的预定义字典，无需手动管理
 */
public class BrotliCompressor extends AbstractCompressor {

    public static final String ALGORITHM_NAME = "Brotli";
    public static final String DESCRIPTION = "Google Brotli 压缩 - 内置优化字典，高压缩率";
    
    private static final byte[] MAGIC = new byte[]{'B', 'R', 'O', 'T'};
    
    // Brotli 压缩级别（1-11，默认 11）
    private final int compressionLevel;

    public BrotliCompressor() {
        this(Deflater.BEST_COMPRESSION);
    }

    public BrotliCompressor(int compressionLevel) {
        super();
        this.compressionLevel = Math.max(1, Math.min(11, compressionLevel));
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION + " (级别: " + compressionLevel + ")";
    }

    @Override
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        startTiming();

        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           BrotliCompressor.compress 开始                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("[原始数据] 长度=" + data.length + " bytes");

        // 创建 Brotli 压缩器
        Deflater deflater = new Deflater(compressionLevel, true);
        
        try {
            deflater.setInput(data);
            deflater.finish();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // 写入 magic header
            baos.write(MAGIC[0]);
            baos.write(MAGIC[1]);
            baos.write(MAGIC[2]);
            baos.write(MAGIC[3]);

            // 压缩数据
            byte[] buffer = new byte[8192];
            while (!deflater.finished()) {
                int len = deflater.deflate(buffer);
                if (len > 0) {
                    baos.write(buffer, 0, len);
                }
            }

            byte[] compressed = baos.toByteArray();
            
            System.out.println("[压缩结果] 压缩后=" + compressed.length + " bytes");
            System.out.println("[压缩比]   " + String.format("%.2f%%", (1 - (double)compressed.length / data.length) * 100));
            
            endTiming(data.length, compressed.length);
            return compressed;
            
        } finally {
            deflater.end();
            System.out.println("╔══════════════════════════════════════════════════════════════════╗");
            System.out.println("║           BrotliCompressor.compress 结束                        ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");
        }
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        if (data == null || data.length < 4) {
            return new byte[0];
        }

        startTiming();

        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           BrotliCompressor.decompress 开始                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("[压缩数据] 长度=" + data.length + " bytes");

        // 验证 magic header
        if (data[0] != MAGIC[0] || data[1] != MAGIC[1] ||
            data[2] != MAGIC[2] || data[3] != MAGIC[3]) {
            throw new IOException("Invalid Brotli compressed file format");
        }

        // 创建 Brotli 解压器
        Inflater inflater = new Inflater(true);
        
        try {
            // 设置压缩数据（跳过 magic header）
            inflater.setInput(data, 4, data.length - 4);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];

            while (!inflater.finished()) {
                try {
                    int len = inflater.inflate(buffer);
                    if (len > 0) {
                        baos.write(buffer, 0, len);
                    }
                } catch (java.util.zip.DataFormatException e) {
                    throw new IOException("Brotli decompression error: " + e.getMessage(), e);
                }
            }

            byte[] decompressed = baos.toByteArray();
            
            System.out.println("[解压结果] 还原后=" + decompressed.length + " bytes");
            
            endTiming(decompressed.length, data.length);
            return decompressed;
            
        } finally {
            inflater.end();
            System.out.println("╔══════════════════════════════════════════════════════════════════╗");
            System.out.println("║           BrotliCompressor.decompress 结束                      ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");
        }
    }

    /**
     * 压缩字符串（便捷方法）
     */
    public byte[] compress(String text) throws IOException {
        return compress(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 解压为字符串（便捷方法）
     */
    public String decompressToString(byte[] data) throws IOException {
        byte[] decompressed = decompress(data);
        return new String(decompressed, StandardCharsets.UTF_8);
    }
}
