package com.luofx.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 说明：
 * 作者：User_luo on 2018/5/8 14:58
 * 邮箱：424533553@qq.com
 */
public class DateUtils {
    public final static String YY_TO_mm = "yyyy-MM-dd HH:mm:ss";//检测数据日期时间格式
    public final static String YY_MM_DD = "yyyy-MM-dd";//检测数据日期时间格式
    public final static String mm_ss = "mm:ss";//检测数据日期时间格式
    public final static String YY_TO_ss = "yyyy-MM-dd HH:mm:ss";//精确到秒的日期时间格式
    private static SimpleDateFormat yyTossFormat = new SimpleDateFormat(YY_TO_ss, Locale.CHINA);
    private static SimpleDateFormat mmssFormat = new SimpleDateFormat(mm_ss, Locale.CHINA);
    private static SimpleDateFormat sampleNoFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
    private static SimpleDateFormat yymmddFormat = new SimpleDateFormat(YY_MM_DD, Locale.CHINA);

    public static String getYY_TO_ss(Date date) {
        return yyTossFormat.format(date);
    }

    public static long getYY_TO_ss(String dateString) {
        try {
            Date date = yyTossFormat.parse(dateString);
            if (date != null) {
                return date.getTime();
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public static String getYY_TO_ss(long date) {
        return yyTossFormat.format(date);
    }

    public static String getSampleNo(Date date) {
        return sampleNoFormat.format(date) + (int) (Math.random() * 9 + 1);
    }

    public static String getSampleNo() {
        return sampleNoFormat.format(new Date());
    }

    public static String getYYMMDD(Date date) {
        return yymmddFormat.format(date);
    }

    public static String getYYMMDD(Long date) {
        return yymmddFormat.format(date);
    }

    public static String getmm_ss(int date) {
        return mmssFormat.format(new Date(date * 1000));
    }


}
