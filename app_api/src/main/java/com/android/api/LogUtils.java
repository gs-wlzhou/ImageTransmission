package com.android.api;

import android.util.Log;

public class LogUtils {

    private static String className; // 类名
    private static String fileName; // 文件名
    private static String methodName; // 方法名
    private static int lineNumber; // 所在行数

    /**
     * 获取方法名,类名,行数,文件名
     * @param elements
     */
    private static void getMethodName(StackTraceElement[] elements) {
        className = elements[1].getClassName();
        fileName = elements[1].getFileName();
        methodName = elements[1].getMethodName();
        lineNumber = elements[1].getLineNumber();
    }

    /**
     * 创建msg
     * @param msg
     * @return
     */
    private static String createMsg(String msg) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(methodName);
        buffer.append("(").append(fileName).append(":").append(lineNumber).append(")=====");
        buffer.append(msg);
        return buffer.toString();
    }

    /**
     * 检查msg是否为null
     * @param msg
     * @return
     */
    private static Object handleMsg(Object msg) {
        if (msg == null) {
            msg = "[null]";
        } else if (msg.toString().trim().length() == 0) {
            msg = "[\"\"]";
        } else {
            msg = msg.toString().trim();
        }
        return msg;
    }

    /**
     * 打印verbose日志
     * @param msg
     */
    public static void v(Object msg) {
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.VERBOSE)) {
            Log.v(className, createMsg(msg.toString()));
        }
    }

    /**
     * 打印debug日志
     * @param msg
     */
    public static void d(Object msg) {
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.DEBUG)) {
            Log.d(className, createMsg(msg.toString()));
        }
    }

    /**
     * 打印info日志
     * @param msg
     */
    public static void i(Object msg) {
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.INFO)) {
            Log.i(className, createMsg(msg.toString()));
        }
    }

    /**
     * 打印warn日志
     * @param msg
     */
    public static void w(Object msg) {
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.WARN)) {
            Log.w(className, createMsg(msg.toString()));
        }
    }

    /**
     * 打印error日志
     * @param msg
     */
    public static void e(Object msg) {
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.ERROR)) {
            Log.e(className, createMsg(msg.toString()));
        }
    }
}