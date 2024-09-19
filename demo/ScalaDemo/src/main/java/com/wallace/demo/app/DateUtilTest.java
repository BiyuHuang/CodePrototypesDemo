/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app;

import com.wallace.demo.app.utils.DateUtil;

import java.text.ParseException;

/**
 * Created by wallace on 2019/4/9.
 */
public class DateUtilTest {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new TestSimpleDateFormatThreadSafe().start();
        }
    }

    public static class TestSimpleDateFormatThreadSafe extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    this.join(2000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }

                try {
                    System.out.println(this.getName() + ":" + DateUtil.parse("2019-11-01 23:30:30"));
                } catch (ParseException pe) {
                    pe.printStackTrace();
                }
            }
        }
    }

}
