package compressor.algorithms;

import compressor.core.AbstractCompressor;
import compressor.model.CompressionStats;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * WebDict 压缩实现 - 使用静态字典 + Trie 匹配
 * 
 * 核心架构：
 * 1. 静态字典（WebDictionary）：预定义高频 Web 字符串，使用 0x80-0xFF 范围索引
 * 2. Trie 匹配：高效查找最长匹配
 * 3. 转义机制：处理高字节（0x80-0xFF）避免与 token 冲突
 */
public class WebDictCompressor extends AbstractCompressor {

    public static final String ALGORITHM_NAME = "WebDict";
    public static final String DESCRIPTION = "网页专用压缩 - 静态字典 + Trie匹配";
    
    private static final byte[] MAGIC = new byte[]{'W', 'D', 'C', 'T'}; // WebDict Compress Token
    private static final int VERSION = 1;

    // 转义前缀（用于表示后续字节是原始数据，而非 token）
    private static final byte ESCAPE_PREFIX = (byte) 0xFF;
    private static final byte ESCAPE_BYTE = 0x00; // 0xFF 0x00 表示后续的 0x80

    // 统计信息
    private int matchCount;
    private int bytesSaved;

    public WebDictCompressor() {
        super();
        resetStats();
    }

    private void resetStats() {
        this.matchCount = 0;
        this.bytesSaved = 0;
    }

    @Override
    public String getAlgorithmName() {
        return ALGORITHM_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public byte[] compress(byte[] data) throws IOException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        startTiming();
        resetStats();

        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           WebDictCompressor.compress 开始                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("[原始数据] 长度=" + data.length + " bytes");

        // 压缩
        byte[] compressed = compressWithDictionary(data);

        // 构建输出
        byte[] result = buildOutput(compressed);

        System.out.println("\n[压缩统计]");
        System.out.println("  - 匹配次数: " + matchCount);
        System.out.println("  - 节省字节: " + bytesSaved);
        System.out.println("[最终结果] 原始=" + data.length + " 压缩后=" + result.length + 
            " 压缩率=" + String.format("%.2f%%", (1 - (double)result.length / data.length) * 100));
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           WebDictCompressor.compress 结束                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");

        endTiming(data.length, result.length);
        return result;
    }

    @Override
    public byte[] decompress(byte[] data) throws IOException {
        if (data == null || data.length < 5) {
            return new byte[0];
        }

        startTiming();

        System.out.println("\n");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           WebDictCompressor.decompress 开始                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("[压缩数据] 长度=" + data.length + " bytes");

        // 验证魔数
        if (data[0] != MAGIC[0] || data[1] != MAGIC[1] ||
            data[2] != MAGIC[2] || data[3] != MAGIC[3]) {
            throw new IOException("Invalid WebDict compressed file format");
        }

        // 提取压缩数据
        byte[] compressedData = new byte[data.length - 5];
        System.arraycopy(data, 5, compressedData, 0, compressedData.length);

        // 解压
        byte[] result = decompressWithDictionary(compressedData);

        System.out.println("\n[解压结果] 还原后=" + result.length + " bytes");
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║           WebDictCompressor.decompress 结束                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝\n");

        endTiming(result.length, data.length);
        return result;
    }

    /**
     * 使用字典进行压缩
     * 
     * 规则：
     * 1. 只匹配多字符词条（长度 >= 2）
     * 2. 高字节（0x80-0xFF）需要转义
     * 3. 只有当词条能节省空间时才使用 token
     */
    private byte[] compressWithDictionary(byte[] data) {
        List<Byte> result = new ArrayList<>();
        int i = 0;

        System.out.println("\n========== 压缩过程 ==========");
        System.out.println("[输入] " + data.length + " bytes");

        while (i < data.length) {
            byte b = data[i];
            
            // 高字节（0x80-0xFF）需要转义
            if (WebDictionary.isToken(b)) {
                result.add(ESCAPE_PREFIX);
                result.add(ESCAPE_BYTE);
                result.add(b);
                i++;
            } else {
                // 尝试在字典中查找匹配
                MatchResult match = TrieMatcher.longestMatch(data, i);
                
                if (match != null && match.matchedText.length() >= 2) {
                    // 只有当词条长度 >= 2 时才使用 token
                    // 否则输出原始字符
                    result.add(match.token);
                    matchCount++;
                    bytesSaved += match.matchedText.length() - 1;
                    
                    System.out.println(String.format("[MATCH  ] pos=%d '%s' -> token=%s 节省=%d",
                        i, escapeForDebug(match.matchedText), 
                        WebDictionary.tokenToString(match.token),
                        match.matchedText.length() - 1));
                    
                    i += match.matchedText.length();
                } else {
                    // 单字符或无匹配，输出原始字节
                    result.add(b);
                    i++;
                }
            }
        }

        byte[] output = new byte[result.size()];
        for (int j = 0; j < result.size(); j++) {
            output[j] = result.get(j);
        }

        System.out.println("[输出] " + output.length + " bytes");
        System.out.println("==============================\n");

        return output;
    }

    /**
     * 使用字典进行解压
     */
    private byte[] decompressWithDictionary(byte[] data) {
        List<Byte> result = new ArrayList<>();
        int i = 0;

        System.out.println("\n========== 解压过程 ==========");
        System.out.println("[输入] " + data.length + " bytes");

        while (i < data.length) {
            byte b = data[i];
            
            // 检查是否是转义序列
            if ((b & 0xFF) == ESCAPE_PREFIX && i + 1 < data.length && (data[i + 1] & 0xFF) == ESCAPE_BYTE) {
                // 转义序列：后续字节是原始数据
                result.add(data[i + 2]);
                System.out.println(String.format("[ESCAPE ] pos=%d [0xFF,0x00,0x%02X] -> 0x%02X",
                    i, data[i + 2] & 0xFF, data[i + 2] & 0xFF));
                i += 3;
            } else if (WebDictionary.isToken(b)) {
                // Token
                String original = WebDictionary.getText(b);
                
                if (original != null) {
                    byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
                    for (byte ob : originalBytes) {
                        result.add(ob);
                    }
                    System.out.println(String.format("[MATCH  ] pos=%d token=%s -> '%s'",
                        i, WebDictionary.tokenToString(b), escapeForDebug(original)));
                    i++;
                } else {
                    // 未知 token
                    result.add(b);
                    i++;
                }
            } else {
                // 普通字节
                result.add(b);
                i++;
            }
        }

        byte[] output = new byte[result.size()];
        for (int j = 0; j < result.size(); j++) {
            output[j] = result.get(j);
        }

        System.out.println("[输出] " + output.length + " bytes");
        System.out.println("=============================\n");

        return output;
    }

    private byte[] buildOutput(byte[] compressedData) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(MAGIC[0]);
        baos.write(MAGIC[1]);
        baos.write(MAGIC[2]);
        baos.write(MAGIC[3]);
        baos.write(VERSION);
        baos.write(compressedData, 0, compressedData.length);
        return baos.toByteArray();
    }

    private static class TrieMatcher {
        public static MatchResult longestMatch(byte[] data, int startPos) {
            if (startPos >= data.length) {
                return null;
            }

            String longestMatch = null;
            byte matchToken = 0;
            int maxLen = 0;

            int maxSearchLen = Math.min(data.length - startPos, 50);

            for (int len = maxSearchLen; len >= 1; len--) {
                String substr = new String(data, startPos, len, StandardCharsets.UTF_8);
                
                if (WebDictionary.contains(substr)) {
                    longestMatch = substr;
                    matchToken = WebDictionary.getToken(substr);
                    maxLen = len;
                    break;
                }
            }

            if (longestMatch != null) {
                return new MatchResult(longestMatch, matchToken);
            }

            return null;
        }
    }

    private static class MatchResult {
        public final String matchedText;
        public final byte token;

        public MatchResult(String matchedText, byte token) {
            this.matchedText = matchedText;
            this.token = token;
        }
    }

    public int getMatchCount() {
        return matchCount;
    }

    public int getBytesSaved() {
        return bytesSaved;
    }

    public String getCompressionInfo() {
        return String.format(
            "WebDict 压缩统计:\n" +
            "  - 匹配次数: %d\n" +
            "  - 节省字节: %d",
            matchCount, bytesSaved
        );
    }

    private static String escapeForDebug(String s) {
        if (s == null) return "(null)";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(s.length(), 30); i++) {
            char c = s.charAt(i);
            if (c < 32) {
                sb.append(String.format("\\x%02X", (int) c));
            } else if (c > 127) {
                sb.append(String.format("\\u%04X", (int) c));
            } else {
                sb.append(c);
            }
        }
        if (s.length() > 30) {
            sb.append("...");
        }
        return sb.toString();
    }
}
