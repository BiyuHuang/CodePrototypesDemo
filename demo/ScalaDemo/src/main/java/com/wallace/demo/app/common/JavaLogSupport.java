package com.wallace.demo.app.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wallace on 2018/12/4.
 */
public interface JavaLogSupport {
    String logSuffixName = "";
    Logger log = LoggerFactory.getLogger(logSuffixName);
}
