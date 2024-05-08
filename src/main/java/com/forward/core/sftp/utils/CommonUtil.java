package com.forward.core.sftp.utils;

import cn.hutool.core.util.StrUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class CommonUtil {



    /**
     * 四舍五入，保留两位小数，不足两位补0
     *
     * @param amtStr
     * @return
     */
    public static String roundHalfUp(String amtStr) {
        if (StringUtil.isBlank(amtStr)) {
            return "0";
        }
        double f = Double.parseDouble(amtStr);
        BigDecimal b = new BigDecimal(f);
        double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

        DecimalFormat decimalFormat = new DecimalFormat("0.00#");
        String format = decimalFormat.format(f1);
        return format;
    }

    //carNo 掩码
    public static String cardNoEncryption(String cardNo) {
        cardNo = cardNo != null ? cardNo.substring(0, 6) + "******" + cardNo.substring(cardNo.length() - 4) : null;
        return cardNo;
    }


    //元转分，去掉小数点
    public static String yuanToFen(String amtStr) {
        if (StringUtil.isBlank(amtStr)) {
            return "0";
        }
        String res = new BigDecimal(amtStr).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString();
        return res;
    }

    /**
     * 获取某天零时的时间
     *
     * @return
     */
    public static Date getZeroDate(int amount) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, amount);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    public static Date getZeroDate(Date date) {
        if (date == null) {
            date = new Date();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR, 0);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime();
    }

    /**
     * 获取昨天的日期
     *
     * @return
     */
    public static Date getYesterDay() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date d = cal.getTime();
        return d;
    }

    /**
     * 获取当前月的上个月的 1号 0时0分0秒
     *
     * @return Date date
     */
    public static Date getLastMonthDate(Date date) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MONTH, -1);//月份减一
        cal.set(Calendar.DAY_OF_MONTH, 1);   //设置为1号,当前日期既为本月第一天
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        date = getZeroDate(cal.getTime());
        return date;
    }

    /**
     * 根据日偏移量和日期获取时间，dayOffset 表示与date的偏移，eg: 1 表示date的下一天，-1表示date的前一天
     *
     * @return Date date
     */
    public static Date getDate(Date date, int dayOffset) {
        if (dayOffset > Integer.MAX_VALUE || dayOffset < Integer.MIN_VALUE) {
            throw new IndexOutOfBoundsException();
        }
        Calendar c = new GregorianCalendar();
        c.setTime(date);
        c.add(Calendar.DAY_OF_YEAR, dayOffset);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        date = getZeroDate(c.getTime());
        return date;
    }

    /**
     * 获取下一天
     */
    public static Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //把日期往后增加一天,整数  往后推,负数往前移动
        date = calendar.getTime(); //这个时间就是日期往后推一天的结果
        return date;
    }

    /**
     * 根据月偏移量和日期获取时间，月偏移量：想要获取的时间的月 - date的月，dayNum：1~31
     *
     * @return Date date
     */
    public static Date getDate(Date date, int monthOffset, int dayNum) {
        if (monthOffset > Integer.MAX_VALUE || monthOffset < Integer.MIN_VALUE
                || dayNum < 1 || dayNum > 31) {
            throw new IndexOutOfBoundsException();
        }
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.MONTH, monthOffset);  //月份偏移
        cal.set(Calendar.DAY_OF_MONTH, dayNum); //设置日期
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        date = getZeroDate(cal.getTime());
        return date;
    }


    public static BigDecimal selectRealAmount(String amtTrans) {
        return new BigDecimal(amtTrans).multiply(new BigDecimal("0.01"));
    }



    public static String interceptTime(String timeStr) {
        if (!StrUtil.isBlank(timeStr)) {
            if (timeStr.contains("T")) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'+'mm:ss", Locale.ENGLISH);
                LocalDateTime ldt = LocalDateTime.parse(timeStr, df);
                ZoneId currentZone = ZoneId.of("UTC");
                ZoneId newZone = ZoneId.of("Asia/Shanghai");
                timeStr = ldt.atZone(currentZone).withZoneSameInstant(newZone).toLocalDateTime().toString();
            }
            if (timeStr.length() >= 10) {
                return timeStr.substring(0, 10);
            }
        }
        return timeStr;
    }

    /**
     * 给 32，33向左填充 0 满足length位
     *
     * @param value
     * @param length
     */
    public static String leftToNumber(String value, int length) {
        if (value.length() == length) {
            return value;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length - value.length(); i++) {
            sb.append("0");
        }
        sb.append(value);
        return sb.toString();
    }

}
