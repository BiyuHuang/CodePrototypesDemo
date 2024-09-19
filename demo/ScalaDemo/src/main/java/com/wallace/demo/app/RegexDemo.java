package com.wallace.demo.app;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Wallace on 2017/1/12.
 */
public class RegexDemo {
    public RegexDemo() {
    }

    public enum Type {
        BOOLEAN, STRING, INT, SHORT, LONG, DOUBLE, LIST, CLASS, PASSWORD
    }

    public static <T> String join(Collection<T> list, String separator) {
        Objects.requireNonNull(list);
        StringBuilder sb = new StringBuilder();
        Iterator<T> iter = list.iterator();
        while (iter.hasNext()) {
            sb.append(iter.next());
            if (iter.hasNext())
                sb.append(separator);
        }
        return sb.toString();
    }

    public static String convertToString(Object value, Type type) {
        if (value == null) {
            return null;
        }
        if (type == null) {
            return value.toString();
        }
        switch (type) {
            case BOOLEAN:
            case SHORT:
            case INT:
            case LONG:
            case DOUBLE:
            case STRING:
            case PASSWORD:
                return value.toString();
            case LIST:
                List<?> valueList = (List<?>) value;
                return join(valueList, ",");
            case CLASS:
                Class<?> clazz = (Class<?>) value;
                return clazz.getName();
            default:
                throw new IllegalStateException("Unknown type.");
        }
    }

    public static void main(String[] args) {
        int[] arr = {9,3,4,6,76,78,9,343,5,45,2,3,232,3456,3,5,4,65,7,6,8,89,8,945646,1235,7,8,6,45,63,4,5,3,4,23,4,2,1,124543,4,57,68,7,9,7,645,6,3,42,4,234,12,31,2,43,765,68,2343,798,7,9,34,7,64,54,96,23534,1,2,3,4,5,6};
        Arrays.sort(arr);
        String str = "test,1,2,3,\"aaa$@=324,bbb=44,ccc=546\",\"454,23,564\",\"$@\",10.934,4534.43";
        System.out.println(str);

        System.out.println(convertToString(1, Type.INT));
        System.out.println(convertToString(true, Type.BOOLEAN));
        String regEx = "\"[a-zA-Z0-9,=$@]+\"";
        Pattern pattern = Pattern.compile(regEx);
        Matcher m = pattern.matcher(str);
        while (m.find()) {
            System.out.println("@@@@@@@@@ " + m.group());
        }
    }
}
