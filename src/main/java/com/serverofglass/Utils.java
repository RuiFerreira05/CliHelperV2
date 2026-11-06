package com.serverofglass;

public class Utils {
    public static String parseCamelCase(String camelCase, boolean lowerCaseTheRest) {
        String camelCaseSeparated = camelCase
                .replaceAll("([a-z])([A-Z])", "$1 $2")
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2");
        if (lowerCaseTheRest) {
            return camelCaseSeparated.substring(0, 1).toUpperCase() + camelCaseSeparated.substring(1).toLowerCase();
        } else {
            return camelCaseSeparated.substring(0, 1).toUpperCase() + camelCaseSeparated.substring(1);
        }
    }

    public static String parseCamelCase(String camelCase) {
        return parseCamelCase(camelCase, false);
    }

    public static String addAtStringIndex(String str, String ch, int position) {
        return str.substring(0, position) + ch + str.substring(position);
    }

    public static <T> T[] reverseArray(T[] arr) {
        T[] newArray = arr.clone();
        for (int i = 0; i < newArray.length / 2; i++) {
            T t = newArray[i];
            newArray[i] = newArray[newArray.length - 1 - i];
            newArray[newArray.length - 1 - i] = t;
        }
        return newArray;
    }
}
