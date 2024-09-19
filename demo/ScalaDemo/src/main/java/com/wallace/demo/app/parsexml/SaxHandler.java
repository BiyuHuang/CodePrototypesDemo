package com.wallace.demo.app.parsexml;

import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by 10192057 on 2018/4/8 0008.
 */
public abstract class SaxHandler extends DefaultHandler {
    abstract public MRO getResult();
}
