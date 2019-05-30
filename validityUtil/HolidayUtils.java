package com.sy.portal.attractInvestmentActivity.subsidyRedPacketReceiveRecord.util;

import com.google.common.collect.Maps;
import com.sy.common.util.HttpClientUtils;
import com.sy.common.util.JsonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Author: lsj
 * @Date: 2019-03-01 17:12
 */
public class HolidayUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
    /**
     * 获取计划激活日期
     * @param today opening date
     * @param num num个工作日后
     * @return
     * @throws ParseException
     */
    public static Date getScheduleActiveDate(Date today, int num) throws ParseException {
        Date tomorrow = null;
        int delay = 1;
        while(delay <= num){
            tomorrow = getTomorrow(today);
            //当前日期+1,判断是否是节假日,不是的同时要判断是否是周末,都不是则scheduleActiveDate日期+1
            if(!isWeekend(sdf.format(tomorrow)) && !isHoliday(sdf2.format(tomorrow))){
                delay++;
                today = tomorrow;
            } else if (isWeekend(sdf.format(tomorrow))){
                today = tomorrow;
            } else if (isHoliday(sdf.format(tomorrow))){
                today = tomorrow;
            }
        }

        return today;
    }

    /**
     * 获取tomorrow的日期
     *
     * @param date
     * @return
     */
    public static Date getTomorrow(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, +1);
        date = calendar.getTime();
        return date;
    }

    /**
     * 判断是否是weekend
     *
     * @param sdate
     * @return
     * @throws ParseException
     */
    public static boolean isWeekend(String sdate) throws ParseException {
        Date date = sdf.parse(sdate);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY){
            return true;
        } else{
            return false;
        }

    }

    /**
     * 判断是否是holiday
     *
     * @param sdate
     * @param list
     * @return
     * @throws ParseException
     */
    public static boolean isHoliday(String sdate, List<String> list) throws ParseException {
        if(list.size() > 0){
            for(int i = 0; i < list.size(); i++){
                if(sdate.equals(list.get(i))){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 判断是否是holiday 节假日
     *
     * @param sdate
     * @return
     * @throws ParseException
     */
    public static boolean isHoliday(String sdate) throws ParseException {
        HttpClientUtils httpClient = HttpClientUtils.getInstance();
        Map<String,String> paramsMap = Maps.newHashMap();
        paramsMap.put("d",sdate);
        //调用api
        String resultString = httpClient.get("http://www.easybots.cn/api/holiday.php", paramsMap);
        System.out.println(resultString);
        Map<String, Object> objectMap = JsonUtils.json2Map(resultString);
        System.out.println(objectMap.get(sdate));
        if(sdate.equals("2")){
            return true;
        }
        return false;
    }




}
