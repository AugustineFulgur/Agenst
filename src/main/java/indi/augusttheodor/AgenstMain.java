package indi.augusttheodor;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;

import java.io.IOException;

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
        } catch (Exception e) {
            System.out.println("[错误] Agenst 运行失败：" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);  //不打算处理但依然写了
        }

    }

    public static void agentmain(String agentArgs, java.lang.instrument.Instrumentation ins) throws Exception {
        //这个方法作为agent时的入口 谁重载
        Options cmd=new Options(agentArgs);
        if(cmd.hasOption("u")){
            org.apache.catalina.core.ExtendServices.SERVICE_URL=cmd.getOptionValue("u")+"E";
            org.apache.catalina.core.RebeyondServices.SERVICE_URL=cmd.getOptionValue("u")+"R";
            org.apache.catalina.core.NeoServices.SERVICE_URL=cmd.getOptionValue("u")+"N";
        }
        if(cmd.hasOption("uR")){
            //使用的时候别为难自己了大哥们
            org.apache.catalina.core.RebeyondServices.SERVICE_URL=cmd.getOptionValue("uR");
        }
        if(cmd.hasOption("uN")){
            org.apache.catalina.core.NeoServices.SERVICE_URL=cmd.getOptionValue("uN");
        }
        if(cmd.hasOption("uE")){
            org.apache.catalina.core.ExtendServices.SERVICE_URL=cmd.getOptionValue("uE");
        }
        java.util.List<indi.augusttheodor.stage.Stage> stages = AttachA.getStages();
        for(indi.augusttheodor.stage.Stage stage : stages){
            stage.configure(cmd);
        }
        TransformerA.init();
        java.util.HashMap<String, String[]> hm = AttachA.findAttachClasses(); //懒得写注释了，要怪请怪VSCODE
        Class[] classes = ins.getAllLoadedClasses();
        //枚举所有已加载类，然后检查我们的注入器中是否包含这些
        for (Class c : classes) {
            if (hm.containsKey(c.getName())) {
                //如果包含呢，那就使用Transformer对其字节码进行注入
                indi.augusttheodor.stage.Stage stage = AttachA.getStageForClass(c.getName());
                if(stage == null){
                    continue;
                }
                for(String mN : hm.get(c.getName())){ //遍历列表取出所有要注入的方法
                    TransformerA tA = new TransformerA(mN,c.getName());
                    tA.setSourceCode(stage.getInjectedSource(c.getName(), mN, cmd)); //前置，在被注入环境内获取所有需要的内容，然后进入agent环境
                    try {
                        java.lang.instrument.ClassDefinition cd=new java.lang.instrument.ClassDefinition(c, tA.getBytes(c));
                        ins.redefineClasses(cd);
                    } catch (java.lang.instrument.UnmodifiableClassException e) {
                        System.out.println(mN+"------"+c.getName()+"------"+"不可注入！");
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

            }
        }
    }

}