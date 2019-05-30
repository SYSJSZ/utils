package com.sy.portal.attractInvestmentActivity.subsidyRedPacketReceiveRecord.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Description:
 * @Author: lsj
 * @Date: 2019-03-01 17:13
 */
public class PersDateUtils {
    public static final String FORMAT_DATE_SPLIT = "yyyy-MM-dd";
    public static final String FORMAT_DATE_NONE = "yyyyMMdd";
    public static final String FORMAT_DATE_TIME = "yyyy-MM-dd HH:mm:ss:SSS";
    public static final String FORMAT_FULL_DATE_TIME = "yyyyMMddHHmmssSSS";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    //获取指定格式
    public static SimpleDateFormat getSdf(String pattern) {
        return new SimpleDateFormat(pattern);
    }

    //获取系统当前时间
    public static Date getDate() {
        return new Date();
    }

    //获取当前日历
    public static Calendar getCalendar() {
        return Calendar.getInstance();
    }

    //获取指定格式的日期sDate
    public static String getPatternDate(String pattern, Date date){
        return getSdf(pattern).format(date);
    }

    //字符串转日期
    public static Date str2Date(String str, SimpleDateFormat sdf) {
        if (null == str || "".equals(str)) {
            return null;
        }
        Date date = null;
        try {
            date = sdf.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
