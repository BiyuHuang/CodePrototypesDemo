/*
 * Copyright (c) 2019. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app;

/**
 * Created by wallace on 2019/4/9.
 */
public class InitClassDemo {

    static InitClassDemo st = new InitClassDemo();
    static int b = 112;

    static {
        System.out.println("1");
    }

    int a = 100;

    {
        System.out.println("2");
    }

    public InitClassDemo() {
        System.out.println("3");
        System.out.println("a=" + a + " b =" + b);
    }

    public static void main(String args[]) {
        staticFunction();
    }

    public static void staticFunction() {
        System.out.println("4");
    }

}
