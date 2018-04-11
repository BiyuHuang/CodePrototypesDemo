package com.wallace.demo.app.common;

import com.wallace.demo.app.parsercombinators.parsers.Parser;

/**
 * Created by 10192057 on 2018/4/11 0011.
 */
public enum ParserType {

    EXTRACT_FIELDS(com.wallace.demo.app.parsercombinators.parsers.ExtractFieldsParser.Builder.class)
    //,SEARCH_REPLACE();
    ;

    private final Class<? extends Parser.Builder> builderClass;

    ParserType(Class<? extends Parser.Builder> builderClass) {
        this.builderClass = builderClass;
    }

    public Class<? extends Parser.Builder> getBuilderClass() {
        return builderClass;
    }
}
