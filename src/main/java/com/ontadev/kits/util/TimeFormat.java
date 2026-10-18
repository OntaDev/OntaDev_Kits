// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeFormat {

    private static final Pattern PART = Pattern.compile("(\\d+)\\s*([a-zA-Zа-яА-Я]+)");

    private TimeFormat() {
    }

    public static String format(long seconds, String day, String hour, String minute, String second) {
        if (seconds <= 0) {
            return "0" + second;
        }

        long d = seconds / 86400;
        long h = (seconds % 86400) / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;

        StringBuilder out = new StringBuilder();
        append(out, d, day);
        append(out, h, hour);
        append(out, m, minute);
        append(out, s, second);
        return out.toString();
    }

    private static void append(StringBuilder out, long value, String unit) {
        if (value <= 0) return;
        if (out.length() > 0) out.append(' ');
        out.append(value).append(unit);
    }

    /** Разбирает ввод: просто число это секунды, иначе части вроде {@code 1d 2h 30m} (или {@code д/ч/м/с}). -1, если не разобралось. */
    public static long parse(String input) {
        if (input == null) return -1;
        String text = input.trim().toLowerCase();
        if (text.isEmpty()) return -1;

        if (text.matches("\\d+")) {
            return Long.parseLong(text);
        }

        Matcher matcher = PART.matcher(text);
        long total = 0;
        int consumed = 0;
        while (matcher.find()) {
            long value = Long.parseLong(matcher.group(1));
            long multiplier = multiplier(matcher.group(2));
            if (multiplier < 0) return -1;
            total += value * multiplier;
            consumed += matcher.group().length();
        }

        if (consumed == 0 || consumed != text.replaceAll("\\s+", "").length()) {
            return -1;
        }
        return total;
    }

    private static long multiplier(String unit) {
        switch (unit) {
            case "d":
            case "д":
                return 86400;
            case "h":
            case "ч":
                return 3600;
            case "m":
            case "м":
                return 60;
            case "s":
            case "с":
                return 1;
            default:
                return -1;
        }
    }
}
