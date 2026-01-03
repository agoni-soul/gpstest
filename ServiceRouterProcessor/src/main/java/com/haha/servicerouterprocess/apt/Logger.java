package com.haha.servicerouterprocess.apt;

/**
 * @auther: haha
 * @Date: 2026/1/3
 * @Detail:
 */
public class Logger {

    private static final String PREFIX = "[DOF_ROUTER_APT] ";

    public static void debug(String s, Object... args) {
        System.out.println(format(s, args));
    }

    public static void info(String s, Object... args) {
        System.out.println(format(s, args));
    }

    public static void warn(String s, Object... args) {
        System.err.println(format(s, args));
    }

    public static void error(String s, Object... args) {
        System.err.println(format(s, args));
    }

    public static void error(Throwable t) {
        t.printStackTrace();
    }

    public static void fatal(Throwable t) {
        t.printStackTrace();
    }

    public static void fatal(String s, Object... args) {
        fatal(new RuntimeException(format(s, args)));
    }

    private static String format(String s, Object... args) {
        return PREFIX + (args.length == 0 ? s : String.format(s, args));
    }
}