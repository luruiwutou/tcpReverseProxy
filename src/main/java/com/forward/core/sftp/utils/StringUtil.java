package com.forward.core.sftp.utils;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    public StringUtil() {
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(String str) {
        return isEmpty(str == null ? "" : str.trim());
    }

    public static boolean isNotBlank(String str) {
        return isNotEmpty(str == null ? "" : str.trim());
    }

    public static String repeat(String str, int repeat) {
        return repeat(str, "", repeat);
    }

    public static String repeat(String str, String separator, int repeat) {
        if (isEmpty(str)) {
            throw new RuntimeException("--------->the param must not be null!");
        } else if (repeat <= 0) {
            return "";
        } else if (repeat > 30) {
            throw new RuntimeException("--------->at must only allowed repeat 30 times");
        } else {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < repeat; ++i) {
                sb.append(str).append(separator);
            }

            if (isNotEmpty(separator)) {
                sb.deleteCharAt(sb.length() - 1);
            }

            return sb.toString();
        }
    }

    public static String rightPad(Object obj, int length, char padChar) {
        if (obj == null) {
            return null;
        } else if (length >= 50) {
            throw new RuntimeException("--------->at most only allowed pad 50 bits");
        } else {
            String str = obj.toString();
            int pads = length - str.length();
            return pads <= 0 ? str : str.concat(repeat(Character.toString(padChar), pads));
        }
    }

    public static String leftPad(Object obj, int length, char leftChar) {
        if (obj == null) {
            return null;
        } else if (length >= 50) {
            throw new RuntimeException("--------->at most only allowed pad 50 bits");
        } else {
            String str = obj.toString();
            int pads = length - str.length();
            return pads <= 0 ? str : repeat(Character.toString(leftChar), pads).concat(str);
        }
    }

    public static int countMatches(String str, String matchStr) {
        if (!isEmpty(str) && !isEmpty(matchStr)) {
            int count = 0;

            for (int idx = 0; (idx = str.indexOf(matchStr, idx)) != -1; idx += matchStr.length()) {
                ++count;
            }

            return count;
        } else {
            return 0;
        }
    }

    public static boolean isDigit(String str) {
        if (str == null) {
            return false;
        } else {
            for (int i = 0; i < str.length(); ++i) {
                if (!Character.isDigit(str.charAt(i))) {
                    return false;
                }
            }

            return true;
        }
    }

    public static boolean isDouble(String str) {
        if (str == null) {
            return false;
        } else {
            try {
                Double.parseDouble(str);
                return true;
            } catch (Exception var2) {
                return false;
            }
        }
    }

    public static boolean regex(String str, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
