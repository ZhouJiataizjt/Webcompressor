package compressor.gui;

import compressor.algorithms.*;
import compressor.core.ICompressor;
import java.util.*;

public class CompressorFactory {

    public enum CompressorType {
        HUFFMAN("Huffman", "哈夫曼编码 - 基于字符频率的变长编码压缩算法"),
        LZ77("LZ77", "LZ77滑动窗口字典压缩 - 基于重复模式的字典编码"),
        BROTLI("Brotli", "Google Brotli压缩 - 内置优化字典，跨平台支持"),
        LZW_IMAGE("LZWImage", "LZW图像压缩 - 基于ZLIB的图像无损像素压缩"),
        POOLING_IMAGE("PoolingImage", "池化降质压缩 - 均值池化+有损压缩，支持质量参数1-10");

        private final String name;
        private final String description;

        CompressorType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final Map<String, CompressorType> TYPE_MAP = new HashMap<>();

    static {
        for (CompressorType type : CompressorType.values()) {
            TYPE_MAP.put(type.getName().toLowerCase(), type);
            TYPE_MAP.put(type.name().toLowerCase(), type);
        }
    }

    public static ICompressor createCompressor(CompressorType type) {
        return switch (type) {
            case HUFFMAN -> new HuffmanCompressor();
            case LZ77 -> new LZ77Compressor();
            case BROTLI -> new BrotliCompressor();
            case LZW_IMAGE -> new LZWImageCompressor();
            case POOLING_IMAGE -> new PoolingImageCompressor();
        };
    }

    public static ICompressor createCompressor(String name) {
        CompressorType type = TYPE_MAP.get(name.toLowerCase());
        if (type == null) {
            throw new IllegalArgumentException("未知的压缩算法: " + name);
        }
        return createCompressor(type);
    }

    /**
     * 根据文件扩展名获取推荐使用的压缩器类型
     * 优先使用 Brotli 算法，因为它是 Google 优化的网页压缩算法
     */
    public static CompressorType getTypeFromExtension(String extension) {
        String ext = extension.toLowerCase();
        return switch (ext) {
            // 网页文本类 - 使用 Brotli（Google优化的网页压缩算法，高效）
            case "html", "htm", "css", "js", "txt", "xml", "json", "svg", "md", "log", "csv" -> CompressorType.BROTLI;
            // 图片类 - 使用池化压缩（有损但压缩率高）
            case "jpg", "jpeg", "png", "gif", "webp", "bmp" -> CompressorType.POOLING_IMAGE;
            // 二进制类 - 统一使用 Brotli（通用高效）
            case "woff", "woff2", "ttf", "eot", "otf", "ico", "bin" -> CompressorType.BROTLI;
            // 其他文件 - 优先使用 Brotli
            default -> CompressorType.BROTLI;
        };
    }

    public static List<CompressorType> getAvailableTypes() {
        return Arrays.asList(CompressorType.values());
    }

    public static CompressorInfo getCompressorInfo(CompressorType type) {
        return new CompressorInfo(type.getName(), type.getDescription());
    }

    public record CompressorInfo(String name, String description) {}
}
