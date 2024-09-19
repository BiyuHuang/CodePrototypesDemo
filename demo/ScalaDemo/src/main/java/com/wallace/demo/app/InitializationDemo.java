/*
 * Copyright (c) 2018. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.wallace.demo.app;

import com.wallace.demo.app.common.JavaLogSupport;

/**
 * Created by wallace on 2018/12/4.
 */

class Grandpa
{
    static
    {
        System.out.println("爷爷在静态代码块");
    }

    public Grandpa() {
        System.out.println("我是爷爷~");
    }
}
class Father extends Grandpa
{
    static
    {
        System.out.println("爸爸在静态代码块");
    }

    public Father()
    {
        System.out.println("我是爸爸~");
    }
}
class Son extends Father
{
    static
    {
        System.out.println("儿子在静态代码块");
    }

    public Son()
    {
        System.out.println("我是儿子~");
    }
}
public class InitializationDemo implements JavaLogSupport
{
    public static void main(String[] args)
    {
        log.info("Class loader mode.");
        new Son();     //入口
    }
}