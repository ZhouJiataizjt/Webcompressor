package compressor.algorithms;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Web 静态字典 - 基于 Brotli 字典设计思想
 * 
 * 核心设计：
 * 1. 精选高频 Web 字符串，总数不超过 128 个
 * 2. 使用 0x80-0xFF 范围作为 token 索引
 * 3. 分类组织：HTML标签、CSS片段、JS关键字、通用文本
 */
public class WebDictionary {

    public static final byte TOKEN_PREFIX = (byte) 0x80;

    // 字典词条数组（共 116 个）
    public static final String[] SEEDS = {
        // HTML 标签 (30)
        "<html>", "</html>", "<head>", "</head>", "<body>", "</body>",
        "<div>", "</div>", "<span>", "</span>", "<p>", "</p>",
        "<a>", "</a>", "<ul>", "</ul>", "<li>", "</li>",
        "<table>", "</table>", "<tr>", "</tr>", "<td>", "</td>",
        "<form>", "</form>", "<script>", "</script>",
        "<style>", "</style>", "<link ", "<meta ",
        
        // 更多标签 (12)
        "<header>", "</header>", "<footer>", "</footer>",
        "<main>", "</main>", "<nav>", "</nav>",
        "<h1>", "</h1>", "<h2>", "</h2>",
        
        // HTML 属性 (16)
        "class=\"", "id=\"", "style=\"", "src=\"", "href=\"",
        "type=\"", "name=\"", "value=\"", "title=\"", "alt=\"",
        "charset=\"utf-8\"", "charset=\"", "rel=\"", "content=\"",
        "placeholder=\"", "disabled",
        
        // CSS (10)
        "display:flex;", "display:block;", "display:none;",
        "background-color:", "color:", "font-size:",
        "margin:", "padding:", "width:", "height:",
        
        // JavaScript (20)
        "function ", "function()", "return ", "var ", "let ", "const ",
        "if (", "else {", "for (", "while (", "case ", "break;",
        "try {", "catch (", "async ", "await ", "null", "true", "false",
        
        // 通用文本 (16)
        "http://", "https://", "www.", "<!--", "-->",
        "text/html", "utf-8", "UTF-8", "text/css",
        "width=device-width", "initial-scale=1",
        
        // 常用英文片段 (12)
        " the ", " and ", " for ", " that ", " with ",
        " not ", " this ", " from ", " have ", " are ",
        " were ", " been "
    };

    private static final Map<String, Byte> FORWARD_MAP = new LinkedHashMap<>();
    private static final Map<Byte, String> REVERSE_MAP = new LinkedHashMap<>();

    static {
        if (SEEDS.length > 128) {
            throw new IllegalStateException("字典词条数超过 128！当前: " + SEEDS.length);
        }
        for (int i = 0; i < SEEDS.length; i++) {
            byte token = (byte) (TOKEN_PREFIX + i);
            FORWARD_MAP.put(SEEDS[i], token);
            REVERSE_MAP.put(token, SEEDS[i]);
        }
        System.out.println("[WebDictionary] 初始化完成，共 " + SEEDS.length + " 个词条");
    }

    public static boolean isToken(byte b) {
        return (b & 0xFF) >= (TOKEN_PREFIX & 0xFF);
    }

    public static String getText(byte token) {
        return REVERSE_MAP.get(token);
    }

    public static byte getToken(String text) {
        Byte token = FORWARD_MAP.get(text);
        if (token == null) throw new IllegalArgumentException("未知字典词条: " + text);
        return token;
    }

    public static boolean contains(String text) {
        return FORWARD_MAP.containsKey(text);
    }

    public static int size() {
        return SEEDS.length;
    }

    public static String[] getSeeds() {
        return SEEDS;
    }

    public static String tokenToString(byte token) {
        if (isToken(token)) {
            return String.format("[0x%02X->%s]", token & 0xFF, getText(token));
        }
        return String.format("[0x%02X]", token & 0xFF);
    }
}
