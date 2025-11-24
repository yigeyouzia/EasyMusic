package com.easymusic.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cyt
 * * @date 2025/11/24 14:46:51
 */

/**
 * 自定义密钥文件读取工具
 * 专门用于读取 /Users/cyt/.keys 文件
 */


public class MacEnvUtils {

    public static void main(String[] args) {
        MacEnvUtils reader = new MacEnvUtils();

        System.out.println("=== 基础用法 ===");
        System.out.println("courseOrderId: " + reader.getValue("courseOrderId"));
    }

    private static final String KEYS_FILE_PATH = "/Users/cyt/.keys";
    private Map<String, String> keyValueMap;
    private long lastModified;

    public MacEnvUtils() {
        this.keyValueMap = new HashMap<>();
        loadKeysFile();
    }

    /**
     * 加载并解析.keys文件
     */
    private void loadKeysFile() {
        try {
            Path keysPath = Paths.get(KEYS_FILE_PATH);

            if (!Files.exists(keysPath)) {
                System.err.println("警告: .keys文件不存在: " + KEYS_FILE_PATH);
                return;
            }

            // 检查文件权限
            if (!Files.isReadable(keysPath)) {
                System.err.println("错误: 没有读取权限: " + KEYS_FILE_PATH);
                return;
            }

            // 记录最后修改时间
            lastModified = Files.getLastModifiedTime(keysPath).toMillis();

            // 读取文件内容
            List<String> lines = Files.readAllLines(keysPath);
            parseKeyValuePairs(lines);

            System.out.println("成功加载 " + keyValueMap.size() + " 个配置项");

        } catch (IOException e) {
            System.err.println("读取.keys文件失败: " + e.getMessage());
        }
    }

    /**
     * 解析键值对
     */
    private void parseKeyValuePairs(List<String> lines) {
        keyValueMap.clear();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // 跳过空行和注释
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            // 解析键值对
            int equalsIndex = line.indexOf('=');
            if (equalsIndex > 0) {
                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();

                // 去除值中的引号（如果存在）
                value = removeQuotes(value);

                if (!key.isEmpty()) {
                    keyValueMap.put(key, value);
                    System.out.println("解析: " + key + " = " +
                            (isSensitiveKey(key) ? "***" : value));
                }
            } else {
                System.out.println("警告: 第 " + (i + 1) + " 行格式错误: " + line);
            }
        }
    }

    /**
     * 去除值中的引号
     */
    private String removeQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) ||
                (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    /**
     * 判断是否为敏感键
     */
    private boolean isSensitiveKey(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("key") || lowerKey.contains("secret") ||
                lowerKey.contains("password") || lowerKey.contains("token");
    }

    /**
     * 获取所有键值对
     */
    public Map<String, String> getAllKeys() {
        return new HashMap<>(keyValueMap);
    }

    /**
     * 获取特定键的值
     */
    public String getValue(String key) {
        return keyValueMap.get(key);
    }

    /**
     * 获取特定键的值，如果不存在返回默认值
     */
    public String getValue(String key, String defaultValue) {
        return keyValueMap.getOrDefault(key, defaultValue);
    }

    /**
     * 检查键是否存在
     */
    public boolean containsKey(String key) {
        return keyValueMap.containsKey(key);
    }

    /**
     * 获取文件路径
     */
    public String getFilePath() {
        return KEYS_FILE_PATH;
    }

    /**
     * 重新加载文件（如果文件被修改过）
     */
    public boolean reloadIfModified() {
        try {
            Path keysPath = Paths.get(KEYS_FILE_PATH);
            if (!Files.exists(keysPath)) {
                return false;
            }

            long currentModified = Files.getLastModifiedTime(keysPath).toMillis();
            if (currentModified > lastModified) {
                System.out.println("检测到文件修改，重新加载...");
                loadKeysFile();
                return true;
            }
        } catch (IOException e) {
            System.err.println("检查文件修改时间失败: " + e.getMessage());
        }
        return false;
    }

    /**
     * 强制重新加载
     */
    public void reload() {
        loadKeysFile();
    }
}
