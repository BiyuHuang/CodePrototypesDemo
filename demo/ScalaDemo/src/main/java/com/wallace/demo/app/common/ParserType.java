package com.wallace.demo.app.common;

import com.wallace.demo.app.parsercombinators.parsers.Builder;

/**
 * Created by 10192057 on 2018/4/11 0011.
 */
public enum ParserType {

    EXTRACT_FIELDS(com.wallace.demo.app.parsercombinators.parsers.ExtractFieldsParser.class)
    //,SEARCH_REPLACE();
    ;

    private final Class<? extends Builder> builderClass;

    ParserType(Class<? extends Builder> builderClass) {
        this.builderClass = builderClass;
    }

    public Class<? extends Builder> getBuilderClass() {
        return builderClass;
    }
}
