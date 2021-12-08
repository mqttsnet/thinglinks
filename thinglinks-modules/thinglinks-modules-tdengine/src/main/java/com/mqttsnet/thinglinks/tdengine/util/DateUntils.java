package com.mqttsnet.thinglinks.tdengine.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Description  时间工具类
 * @author  mqtts.net
 * @Email 13733918655@163.com
 * @Date 2019/12/9 20:45
 * @Version 1.0
 */
public class DateUntils {

    public static Date getCurrentDate() {
        return new Date();
    }

    public static String getDateString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String previousDate = dateFormat.format(date);
        return previousDate;
    }

    public static String getDateStrings(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String previousDate = dateFormat.format(date);
        return previousDate;
    }

    public static String getDateStampString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        String previousDate = dateFormat.format(date);
        return previousDate;
    }

    public static Date getDateString(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = null;
        try {
            date1 = dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date1;
    }

    public static String strToDateFormat(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        formatter.setLenient(false);
        Date newDate = formatter.parse(date);
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(newDate);
    }

    public static String getDateFormatString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String previousDate = dateFormat.format(date);
        return previousDate;
    }

    public static String getDateFormatTostr(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String previousDate = dateFormat.format(date);
        return previousDate;
    }

    public static String getCurrentDateWithZero(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String previousDate = dateFormat.format(date);
        return previousDate;
    }

    public static String getCurrentDateWithNight(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        String previousDate = dateFormat.format(date);
        return previousDate;
    }

    public static String getPreviousDateWithZero(Date date) {
        Date prevDate = new Date(date.getTime() - (24 * 3600000));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String previousDate = dateFormat.format(prevDate);
        return previousDate;
    }

    public static Date getFomatDate(String str) {
        //创建SimpleDateFormat对象实例并定义好转换格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            // 注意格式需要与上面一致，不然会出现异常
            date = sdf.parse(str);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * 将时间戳转换为日期
     *
     * @param stamp 时间戳
     * @return 时间，返回格式为 yyyy-MM-dd-HH-mm-ss
     */
    public static String Stamp2Date(Long stamp) {
        String result = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(stamp);
        return result;
    }

    /**
     * 取得当前时间戳（精确到秒）
     *
     * @return
     */
    public static String timeStamp() {
        long time = System.currentTimeMillis();
        String t = String.valueOf(time / 1000);
        return t;
    }

    /**
     * 取得当前时间戳（精确到秒）Long 类型
     *
     * @return
     */
    public static Long timeStampL() {
        long time = System.currentTimeMillis();
        return time / 1000;
    }

    /**
     * 获取指定url中的某个参数
     *
     * @param url
     * @param name
     * @return
     */
    public static String getParamByUrl(String url, String name) {
        url += "&";
        String pattern = "(\\?|&){1}#{0,1}" + name + "=[a-zA-Z0-9]*(&{1})";

        Pattern r = Pattern.compile(pattern);

        Matcher m = r.matcher(url);
        if (m.find()) {
            System.out.println(m.group(0));
            return m.group(0).split("=")[1].replace("&", "");
        } else {
            return null;
        }
    }
    public static void main(String[] args) throws ParseException {

        String sTime = "2019-05-17 00:00:00";
        String eTime = "2019-05-19 16:00:00";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = getFomatDate(sTime);
        Date d2 = getFomatDate(eTime);
        List<String> times = getTimes(d1,d2,3600000L);
        for (String str:times) {
            System.out.println("得到的时间集合=:"+str);
        }
        List<String> times1 = getTimes(getFomatDate("2021-09-22 00:00:00"), getFomatDate("2021-09-30 00:00:00"), 86400000);
        System.out.println(times1);
    }


    //获取时间段内,时间间隔的所以时间点集合
    public static List<String> getTimes(Date startTime, Date endTime,long ll) throws ParseException {

        List<String> times = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startTime);
        long startTimeStamp = calendar.getTimeInMillis();//起始的毫秒数
        calendar.setTime(endTime);
        long endTimeStamp = calendar.getTimeInMillis();//截止时间毫秒数

        while (true){
            long temp = startTimeStamp + ll;
            if(temp <= endTimeStamp){
                startTimeStamp = temp;
                Calendar _calendar = Calendar.getInstance();
                _calendar.setTimeInMillis(temp);
                int year = _calendar.get(Calendar.YEAR);
                int month = _calendar.get(Calendar.MONTH);
                int day = _calendar.get(Calendar.DAY_OF_MONTH);
                int hour = _calendar.get(Calendar.HOUR_OF_DAY);//24小时制
                //int hour = calendar.get(Calendar.HOUR);//12小时制
                int minute = _calendar.get(Calendar.MINUTE);
                int second  = _calendar.get(Calendar.SECOND);
                String time = year + "-" + (month + 1) + "-" + day + " " + hour + ":" + minute + ":" + second;
                //时间格式处理
                String res;
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = simpleDateFormat.parse(time);
                long ts = date.getTime();
                long lt = new Long(String.valueOf(ts));
                Date date1 = new Date(lt);
                res = simpleDateFormat.format(date1);
                times.add(res);
            }else {
                break;
            }
        }
        return times;
    }
    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s) throws ParseException {
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }

    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }

}
