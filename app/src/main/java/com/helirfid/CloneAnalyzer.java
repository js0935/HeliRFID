package com.helirfid;

public class CloneAnalyzer {

    public static String analyze(String uid, String card10) {
        if (uid == null || uid.isEmpty()) {
            return "缺少 UID";
        }

        uid = uid.replace(":", "");
        
        StringBuilder result = new StringBuilder();
        result.append("複製風險分析:\n");

        // 檢查常見的低安全性 UID 模式
        if (uid.startsWith("04") && uid.length() == 16) {
            result.append("⚠️ UID 為標準 UID-4 格式\n");
            result.append("   (04 開頭，16 位元組)\n");
        } else if (uid.startsWith("E0") && uid.length() == 16) {
            result.append("⚠️ UID 為標準 UID-7 格式\n");
            result.append("   (E0 開頭，16 位元組)\n");
        }

        // 檢查是否包含連續相同數字或規律模式
        if (hasSequentialPattern(uid) || hasIdenticalBytes(uid)) {
            result.append("⚠️ UID 包含規律模式\n");
            result.append("   可能容易被複製\n");
        } else {
            result.append("✓ UID 無明顯規律模式\n");
        }

        // 檢查卡片號是否重複
        if (card10 != null && card10.length() == 10) {
            if (isCommonCardNumber(card10)) {
                result.append("⚠️ 卡號較常見\n");
                result.append("   碰撞風險較高\n");
            } else {
                result.append("✓ 卡號較為獨特\n");
            }
        }

        // 最終風險評估
        String risk = assessOverallRisk(uid);
        result.append("\n整體風險評估: ").append(risk).append("\n");

        return result.toString();
    }

    private static boolean hasSequentialPattern(String hex) {
        String upper = hex.toUpperCase();
        for (int i = 0; i < upper.length() - 2; i += 2) {
            String current = upper.substring(i, i + 2);
            String next = upper.substring(i + 2, i + 4);
            if (isSequentialHex(current, next)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSequentialHex(String hex1, String hex2) {
        try {
            int val1 = Integer.parseInt(hex1, 16);
            int val2 = Integer.parseInt(hex2, 16);
            return val2 == val1 + 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean hasIdenticalBytes(String hex) {
        String upper = hex.toUpperCase();
        for (int i = 0; i < upper.length() - 2; i += 2) {
            String current = upper.substring(i, i + 2);
            for (int j = i + 2; j < upper.length(); j += 2) {
                String next = upper.substring(j, j + 2);
                if (current.equals(next)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean isCommonCardNumber(String card10) {
        try {
            long num = Long.parseLong(card10);
            return num < 1000000000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String assessOverallRisk(String uid) {
        if (uid.startsWith("04") || uid.startsWith("E0")) {
            return "中等風險（標準 UID 格式）";
        }
        
        if (hasSequentialPattern(uid) || hasIdenticalBytes(uid)) {
            return "高風險（有規律模式）";
        }

        return "低風險（UID 看起正常）";
    }

    public static String getRecommendation(String riskLevel) {
        if (riskLevel.contains("高風險")) {
            return "建議：使用更安全的卡片或結合其他驗證方式";
        } else if (riskLevel.contains("中等風險")) {
            return "建議：定期更換卡片 UID";
        } else if (riskLevel.contains("低風險")) {
            return "建議：保持正常使用";
        }
        return "建議：請根據實際風險評估採取對策";
    }

    public static boolean isPotentiallyCloned(String uid) {
        uid = uid.replace(":", "");
        return hasSequentialPattern(uid) || hasIdenticalBytes(uid);
    }

    public static boolean isStandardUID(String uid) {
        uid = uid.replace(":", "");
        return uid.startsWith("04") || uid.startsWith("E0");
    }
}
