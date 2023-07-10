package com.forward.core.sftp.utils;


import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期工具类
 */
public class DateUtil {
    // ==格式到年==
    /**
     * 日期格式，年份，例如：2004，2008
     */
    public static final String DATE_FORMAT_YYYY = "yyyy";

    // ==格式到年月 ==
    /**
     * 日期格式，年份和月份，例如：200707，200808
     */
    public static final String DATE_FORMAT_YYYYMM = "yyyyMM";

    /**
     * 日期格式，年份和月份，例如：200707，2008-08
     */
    public static final String DATE_FORMAT_YYYY_MM = "yyyy-MM";


    // ==格式到年月日==
    /**
     * 日期格式，年月日，例如：050630，080808
     */
    public static final String DATE_FORMAT_YYMMDD = "yyMMdd";

    /**
     * 日期格式，年月日时分 例如：0506301028
     */
    public static final String DATE_FORMAT_YYMMDDHHMM = "yyMMddHHmm";

    /**
     * 日期格式，年月日，用横杠分开，例如：06-12-25，08-08-08
     */
    public static final String DATE_FORMAT_YY_MM_DD = "yy-MM-dd";

    /**
     * 日期格式，年月日，例如：20050630，20080808
     */
    public static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";

    /**
     * 日期格式，年月日，用横杠分开，例如：2006-12-25，2008-08-08
     */
    public static final String DATE_FORMAT_YYYY_MM_DD = "yyyy-MM-dd";

    /**
     * 日期格式，年月日，例如：2016.10.05
     */
    public static final String DATE_FORMAT_POINTYYYYMMDD = "yyyy.MM.dd";

    /**
     * 日期格式，年月日，例如：2016年10月05日
     */
    public static final String DATE_TIME_FORMAT_YYYY年MM月DD日 = "yyyy年MM月dd日";


    // ==格式到年月日 时分 ==

    /**
     * 日期格式，年月日时分，例如：200506301210，200808081210
     */
    public static final String DATE_FORMAT_YYYYMMDDHHmm = "yyyyMMddHHmm";

    /**
     * 日期格式，年月日时分，例如：20001230 12:00，20080808 20:08
     */
    public static final String DATE_TIME_FORMAT_YYYYMMDD_HH_MI = "yyyyMMdd HH:mm";

    /**
     * 日期格式，年月日时分，例如：2000-12-30 12:00，2008-08-08 20:08
     */
    public static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI = "yyyy-MM-dd HH:mm";

    // ==格式到月日 时分秒==
    /**
     * 日期格式，月日时分秒，例如：0808200808
     */
    public static final String DATE_TIME_FORMAT_MMDDHHMISS = "MMddHHmmss";

    // ==格式到月日==
    /**
     * 日期格式，月日，例如：0808
     */
    public static final String DATE_TIME_FORMAT_MMDD = "MMdd";

    // ==格式到年月日 时分秒==
    /**
     * 日期格式，年月日时分秒，例如：20001230120000，20080808200808
     */
    public static final String DATE_TIME_FORMAT_YYYYMMDDHHMISS = "yyyyMMddHHmmss";

    /**
     * 日期格式，年月日时分秒，年月日用横杠分开，时分秒用冒号分开
     * 例如：2005-05-10 23：20：00，2008-08-08 20:08:08
     */
    public static final String DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * 二位年份
     */
    public static final String DATE_FORMAT_YY = "yy";


    // ==格式到年月日 时分秒 毫秒==
    /**
     * 日期格式，年月日时分秒毫秒，例如：20001230120000123，20080808200808456
     */
    public static final String DATE_TIME_FORMAT_YYYYMMDDHHMISSSSS = "yyyyMMddHHmmssSSS";


    // ==特殊格式==
    /**
     * 日期格式，月日时分，例如：10-05 12:00
     */
    public static final String DATE_FORMAT_MMDDHHMI = "MM-dd HH:mm";
    /**
     * 日期格式，年月日时分秒，例如：2018/9/3 17:35:49
     */
    public static final String DATE_FORMAT_YYYYMMDDHHMMSS = "yyyy/MM/dd HH:mm:ss";

    /**
     * @param date    日期对象
     * @param pattern 转换格式
     * @return
     * @author baojunjun
     * 转移日期为日期字符串
     */
    public static String format(Date date, String pattern) {
        LocalDateTime localDateTime = dateConvertLocalDateTime(date);
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 获取年份+天数
     *
     * @return
     */
    public static String getJulianDays() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int year = calendar.get(Calendar.YEAR) - 1900;
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        String juLianDate = String.valueOf(year * 1000 + dayOfYear);
        return juLianDate.substring(juLianDate.length() - 5);
    }

    /**
     * @param dateStr 日期字符串
     * @param pattern 转换格式
     * @return
     * @author baojunjun
     * 字符串转换成指定格式的日期对象
     */
    public static Date parse(String dateStr, String pattern) {
        if (pattern.contains("HH") || pattern.contains("mm") || pattern.contains("ss")) {
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            return localDateTimeConvertDate(localDateTime);
        } else {
            LocalDate localDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        }
    }

    /**
     * @param str 日期字符串
     * @return
     * @author 字符串转换成指定格式的日期对象
     */
    public static Date parseDate(String str, String pattern) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        Date currentTime = formatter.parse(str);
        return currentTime;
    }

    /**
     * @param date 日期对象
     * @return LocalDateTime
     * @author baojunjun
     * 日期转换成LocalDateTime对象
     */
    public static LocalDateTime dateConvertLocalDateTime(Date date) {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * @param localDateTime
     * @return Date
     * @author baojunjun
     * LocalDateTime转换成date
     */
    public static Date localDateTimeConvertDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @param date     日期格式
     * @param dateUnit 时间单位
     * @param interval 间隔数
     * @return Date
     * @author baojunjun
     * 获取指定日期的间隔日期，可以是之前或之后
     * 如3天前的日期使用getIntervalDate(new Date (), DateUnit.DAY,-3)
     * 天后的日期使用getIntervalDate(new Date (), DateUnit.DAY, 3)
     */
    public static Date getIntervalDate(Date date, DateUnit dateUnit, long interval) {
        LocalDateTime localDateTime = dateConvertLocalDateTime(date);
        switch (dateUnit) {
            case YEAR:
                localDateTime = localDateTime.plusYears(interval);
                break;
            case MONTH:
                localDateTime = localDateTime.plusMonths(interval);
                break;
            case DAY:
                localDateTime = localDateTime.plusDays(interval);
                break;
            case HOUR:
                localDateTime = localDateTime.plusHours(interval);
                break;
            case MINUTE:
                localDateTime = localDateTime.plusMinutes(interval);
                break;
            case SECOND:
                localDateTime = localDateTime.plusSeconds(interval);
                break;
            case WEEK:
                localDateTime = localDateTime.plusWeeks(interval);
                break;
        }
        return localDateTimeConvertDate(localDateTime);
    }

    /**
     * @param date     日期格式
     * @param dateUnit 时间单位
     * @param interval 间隔数
     * @param pattern  转换格式
     * @return Date
     * @author baojunjun
     * 获取指定日期的间隔日期，可以是之前或之后
     * 如3天前的日期使用getIntervalDate(new Date (), DateUnit.DAY,-3)
     * 3天后的日期使用getIntervalDate(new Date (), DateUnit.DAY, 3)
     */
    public static String getIntervalDateStr(Date date, DateUnit dateUnit, long interval, String pattern) {
        date = getIntervalDate(date, dateUnit, interval);
        return format(date, pattern);
    }

    /**
     * @return Date
     * @author baojunjun
     * 获取昨天的日期
     */
    public static Date getYesterdayDate() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTimeConvertDate(localDateTime.minusDays(1));
    }

    /**
     * @param pattern 转换格式
     * @return String
     * @author baojunjun
     * 获取昨天的日期格式字符串
     */
    public static String getYesterdayDateStr(String pattern) {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(1);
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * @return Date
     * @author baojunjun
     * 获取明天的日期
     */
    public static Date getTomorrow() {
        LocalDateTime localDateTime = LocalDateTime.now();
        return localDateTimeConvertDate(localDateTime.plusDays(1));
    }

    /**
     * @param pattern 转换格式
     * @return String
     * @author baojunjun
     * 获取明天的日期格式字符串
     */
    public static String getTomorrowDateStr(String pattern) {
        LocalDateTime localDateTime = LocalDateTime.now().plusDays(1);
        return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * 根据步进调整日期
     *
     * @param date
     * @param field Calendar.field，例如Calendar.SECOND, Calendar.MONTH
     * @param step
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date addDateField(Date date, int field, int step) {
        Calendar cal = Calendar.getInstance();
        if (date != null) {
            cal.setTime(date);
        }
        cal.add(field, step);
        return cal.getTime();
    }

    /**
     * 根据步进调整日期
     *
     * @param date
     * @param field Calendar.field，例如Calendar.SECOND, Calendar.MONTH
     * @param step
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date subtractDateField(Date date, int field, int step) {
        return addDateField(date, field, step * (-1));
    }

    /**
     * 调整日期的秒数
     *
     * @param date
     * @param seconds 为null则默认为当前日期
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date addSeconds(Date date, int seconds) {
        return addDateField(date, Calendar.SECOND, seconds);
    }

    /**
     * 调整日期的分钟数
     *
     * @param date
     * @param minutes 为null则默认为当前日期
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date addMinutes(Date date, int minutes) {
        return addDateField(date, Calendar.MINUTE, minutes);
    }

    /**
     * 调整日期的小时数
     *
     * @param date  为null则默认为当前日期
     * @param hours
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date addHours(Date date, int hours) {
        return addDateField(date, Calendar.HOUR, hours);
    }

    /**
     * 调整日期的天数
     *
     * @param date 为null则默认为当前日期
     * @param days 步进
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date addDays(Date date, int days) {
        return addDateField(date, Calendar.DATE, days);
    }

    /**
     * 调整日期的天数
     *
     * @param date 为null则默认为当前日期
     * @param days 步进
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date subtractDays(Date date, int days) {
        return subtractDateField(date, Calendar.DATE, days);
    }

    /**
     * 调整日期的月份
     *
     * @param date   为null则默认为当前日期
     * @param months 步进
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date addMonths(Date date, int months) {
        return addDateField(date, Calendar.MONTH, months);
    }

    /**
     * 调整日期的年份
     *
     * @param date  为null则默认为当前日期
     * @param years 步进
     * @return
     * @author cj
     * 2018-09-27
     */
    public static Date addYears(Date date, int years) {
        return addDateField(date, Calendar.YEAR, years);
    }

    public static String date10MMDDHHmmss() {
        return date10MMDDHHmmss(new Date());
    }

    public static String date10MMDDHHmmss(Date date) {
        return DateUtil.format(date, "MMddHHmmss");
    }

    public static String time6() {
        return time6(new Date());
    }

    /**
     * 返回 10位长度的当前日期格式  yyMMDDHHmm
     *
     * @return
     */
    public static String time6(Date date) {
        return DateUtil.format(date, "HHmmss");
    }

    /**
     * 返回 10位长度的当前日期格式  yyMMDDHHmm
     *
     * @return
     */
    public static String date4() {
        return date4(new Date());
    }


    /**
     * 返回 10位长度的当前日期格式  yyMMDDHHmm
     *
     * @return
     */
    public static String date4(Date date) {
        return DateUtil.format(date, "MMdd");
    }


    /**
     * 时间戳转换成日期格式字符串
     *
     * @param seconds
     * @param format
     * @return
     * @author cj
     * 2018-10-25
     */
    public static String timeStamp2Date(String seconds, String format) {
        if (seconds == null || seconds.isEmpty() || seconds.equals("null")) {
            return "";
        }
        if (format == null || format.isEmpty()) {
            format = DATE_TIME_FORMAT_YYYY_MM_DD_HH_MI_SS;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds)));
    }

    /**
     * @param strDate
     * @param format
     * @return
     * @Author zhouzhb
     * @Description 根据format转换成日期
     * @Date 2019-03-21 11:13
     */
    public static Date strToDate(String strDate, String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(strDate, new ParsePosition(0));
    }

    //Date yyyyMMdd -> String
    public static String dateToStr(Date date, String formatStr) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return format.format(date);
    }

    // 時間格式(MMDDhhmmss)
    public static String RespsoneDate() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        return month + "" + day + "" + hour + "" + minute + "" + second + "";

    }

    // 時間格式(hhmmss)
    public static String RespsoneHour() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int second = cal.get(Calendar.SECOND);
        return hour + "" + minute + "" + second + "";

    }

    // 時間格式(MMDD)
    public static String RespsoneDay() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return month + "" + day;

    }

    // 時間格式(YYMM)
    public static String RespsoneYear() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return year + "" + month;

    }

    // 時間格式(YYMMDD)
    public static String yearMonthDay() {
        Calendar cal = Calendar.getInstance();
        return DateUtil.format(cal.getTime(), DateUtil.DATE_FORMAT_YYYYMMDD);

    }

    public static LocalDateTime UDateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static LocalTime UDateToLocalTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return localDateTime.toLocalTime();
    }

    public static void main(String[] args) {
        System.out.println(yearMonthDay());
//        Date date = DateUtil.strToDate("20131104", "yyyyMMdd");
//        System.out.println(date.toString());
//        System.out.println(DateUtil.parse("20170102142312", "yyyyMMddHHmmss"));
/*        System.out.println(DateUtil.format(new Date(),"yyyy-MM-dd"));
        System.out.println(dateConvertLocalDateTime(new Date()));
        System.out.println(localDateTimeConvertDate(LocalDateTime.now()));
        System.out.println(getYesterdayDate());
        System.out.println(getTomorrow());
        System.out.println("------------------------------");
        System.out.println(getIntervalDate(new Date(),DateUnit.YEAR,1));
        System.out.println(getIntervalDateStr(new Date(),DateUnit.DAY,-5,"yyyy-MM-dd HH:mm:ss"));*/
        //System.out.println(parse("2018/9/3 17:35:49", DateUtil.DATE_FORMAT_YYYYMMDDHHMMSS));
        //System.out.println(parse("2018-09-10 11:00:00", "yyyy/MM/dd HH:mm:ss"));
        //System.out.println(strToDate("2018-12-24",DateUtil.DATE_FORMAT_YYYYMMDDHHMMSS));

    }    // 获取下一天

    public static Date getNextDay(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, 1); //把日期往后增加一天,整数  往后推,负数往前移动
        date = calendar.getTime(); //这个时间就是日期往后推一天的结果
        return date;
    }

    /**
     * 获取前一天日期
     *
     * @param format
     * @return
     */
    public static String getYesterday(String format) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        Date d = cal.getTime();
        SimpleDateFormat sp = new SimpleDateFormat(format);
        return sp.format(d);
    }

    private enum DateUnit {
        YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, WEEK
    }
}

