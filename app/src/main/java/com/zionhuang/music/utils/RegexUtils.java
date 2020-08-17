package com.zionhuang.music.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {
    public static boolean find(String regex, String s) {
        return Pattern.compile(regex).matcher(s).find();
    }

    public static String search(String regex, CharSequence s) {
        return search(regex, s, null);
    }

    public static String search(String regex, CharSequence s, String group) {
        Matcher m = Pattern.compile(regex).matcher(s);
        if (m.find()) {
            if (group == null) {
                return m.groupCount() > 0 ? m.group(1) : m.group(0);
            } else {
                return m.group(group);
            }
        }
        return "";
    }

    public static String search(String[] regExes, CharSequence s) {
        return search(regExes, s, null);
    }

    public static String search(String[] regExes, CharSequence s, String group) {
        Matcher m;
        for (String regex : regExes) {
            m = Pattern.compile(regex).matcher(s);
            if (m.find()) {
                if (group == null) {
                    return m.groupCount() > 0 ? m.group(1) : m.group(0);
                } else {
                    return m.group(group);
                }
            }
        }
        return "";
    }
}
