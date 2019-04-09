/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wallace on 2019/4/9.
 */
public class DateUtil {

    @SuppressWarnings("Non-Thread-Safe")
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //使用ThreadLocal，将共享变量变为独享，线程独享比方法独享在并发环境下能减少不少创建对象的开销
    private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    public static String formatDate(Date date) throws ParseException {
        //        return sdfsdf.format(date);
        return threadLocal.get().format(date);
    }

    public static Date parse(String strDate) throws ParseException {
        //        return sdf.parse(strDate);
        return threadLocal.get().parse(strDate);

    }
}
