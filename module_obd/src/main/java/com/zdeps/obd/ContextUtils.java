package com.zdeps.obd;

import android.content.Context;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 通过反射获取到context对象
 */
public class ContextUtils {

    private static Context CONTEXT_INSTANCE;

    public static Context getContext() {


        if (null == CONTEXT_INSTANCE) {
            synchronized (ContextUtils.class) {

                try {
                    Class<?> ActivityThread = Class.forName("android.app.ActivityThread");

                    Method method = ActivityThread.getMethod("currentActivityThread");

                    Object currentActivityThread = method.invoke(ActivityThread);


                    Method method1 = currentActivityThread.getClass().getMethod("getApplication");

                    CONTEXT_INSTANCE = (Context) method1.invoke(currentActivityThread);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return CONTEXT_INSTANCE;
    }
}
