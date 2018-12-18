package sample_rr.core.utils;

public class StringUtils {
    public static String sanitize(String name){
        return name.replaceAll("\\s+","").toLowerCase();
    }
}