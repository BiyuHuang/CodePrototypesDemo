package com.wallace.demo.app.common;

import com.wallace.demo.app.parsercombinators.parsers.AbstractParser;

/**
 * Created by 10192057 on 2018/4/11 0011.
 */
public enum ParserType {

    EXTRACT_FIELDS(com.wallace.demo.app.parsercombinators.parsers.ExtractFieldsAbstractParser.class)
    //,SEARCH_REPLACE();
    ;

    private final Class<? extends AbstractParser> builderClass;

    ParserType(Class<? extends AbstractParser> builderClass) {
        this.builderClass = builderClass;
    }

    public Class<? extends AbstractParser> getBuilderClass() {
        return builderClass;
    }
}
