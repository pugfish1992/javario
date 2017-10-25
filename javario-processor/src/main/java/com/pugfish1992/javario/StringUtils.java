package com.pugfish1992.javario;

/**
 * Created by daichi on 10/25/17.
 */

class StringUtils {

    private StringUtils() {}

    /**
     * Convert : anyCamelCaseText -> ANY_CAMEL_CASE_TEXT
     */
    static String camelToCapitalSnake(String camel) {
        String capSnake = "";
        for (int i = 0; i < camel.length(); ++i) {
            char c = camel.charAt(i);
            capSnake += (Character.isUpperCase(c)) ? "_" + c : c;
        }
        return capSnake.toUpperCase();
    }
}
