package com.android.api;

import android.util.Log;

public class LogUtils {

    private static boolean open = true; // 日志工具是否开启，默认为true
    private static String className; // 类名
    private static String methodName; // 方法名
    private static int lineNumber; // 所在行数

    public static boolean isOpen() {
        return open;
    }

    public static void setOpen(boolean open) {
        LogUtils.open = open;
    }

    /**
     * 获取方法名，类名，行数
     * @param elements
     */
    private static void getMethodName(StackTraceElement[] elements) {
        className = elements[1].getFileName();
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
        buffer.append("(").append(className).append(":").append(lineNumber).append(")");
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

    public static void v(Object msg) {
        if (!open) {
            return;
        }
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.VERBOSE)) {
            Log.v(className, createMsg(msg.toString()));
        }
    }

    public static void d(Object msg) {
        if (!open) {
            return;
        }
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.DEBUG)) {
            Log.d(className, createMsg(msg.toString()));
        }
    }

    public static void i(Object msg) {
        if (!open) {
            return;
        }
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.INFO)) {
            Log.i(className, createMsg(msg.toString()));
        }
    }

    public static void w(Object msg) {
        if (!open) {
            return;
        }
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.WARN)) {
            Log.w(className, createMsg(msg.toString()));
        }
    }

    public static void e(Object msg) {
        if (!open) {
            return;
        }
        msg = handleMsg(msg);
        getMethodName(new Throwable().getStackTrace());
        if (Log.isLoggable("Mms:transaction", Log.ERROR)) {
            Log.e(className, createMsg(msg.toString()));
        }
    }
}