package com.wallace.demo.app;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Wallace on 2017/1/12.
 */
public class RegexDemo {
    public RegexDemo() {
    }

    public static void main(String[] args) {
        String str = "test,1,2,3,\"aaa$@=324,bbb=44,ccc=546\",\"454,23,564\",\"$@\",10.934,4534.43";
        System.out.println(str);

        String regEx = "\"[a-zA-Z0-9,=$@]+\"";
        Pattern pattern = Pattern.compile(regEx);
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            System.out.println("@@@@@@@@@ " + m.group());
        }
    }
}
