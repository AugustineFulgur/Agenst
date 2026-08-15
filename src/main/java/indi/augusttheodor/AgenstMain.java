package indi.augusttheodor;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;

import indi.augusttheodor.stage.Stage;
import org.apache.catalina.core.ExtendServices;
import org.apache.catalina.core.NeoServices;
import org.apache.catalina.core.RebeyondServices;
import java.io.IOException;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.HashMap;
import java.util.List;

public class AgenstMain {

    public static void main(String[] args){
        try{
            String edit="";
            for(String a:args){
                edit+=a;
                edit+="\n";
            }
            AttachA.doAttach(edit);
        } catch (AgentLoadException | IOException | AgentInitializationException e) {
            throw new RuntimeException(e);
        }

    }

    public static void agentmain(String agentArgs, Instrumentation ins) throws Exception {
        Options cmd=new Options(agentArgs);
        if(cmd.hasOption("u")){
            ExtendServices.SERVICE_URL=cmd.getOptionValue("u")+"E";
            RebeyondServices.SERVICE_URL=cmd.getOptionValue("u")+"R";
            NeoServices.SERVICE_URL=cmd.getOptionValue("u")+"N";
        }
        if(cmd.hasOption("uR")){
            RebeyondServices.SERVICE_URL=cmd.getOptionValue("uR");
        }
        if(cmd.hasOption("uN")){
            NeoServices.SERVICE_URL=cmd.getOptionValue("uN");
        }
        if(cmd.hasOption("uE")){
            ExtendServices.SERVICE_URL=cmd.getOptionValue("uE");
        }
        List<Stage> stages = AttachA.getStages();
        for(Stage stage : stages){
            stage.configure(cmd);
        }
        TransformerA.init();
        HashMap<String, String[]> hm = AttachA.findAttachClasses();
        Class[] classes = ins.getAllLoadedClasses();
        for (Class c : classes) {
            if (hm.containsKey(c.getName())) {
                Stage stage = AttachA.getStageForClass(c.getName());
                if(stage == null){
                    continue;
                }
                for(String mN : hm.get(c.getName())){
                    TransformerA tA = new TransformerA(mN,c.getName());
                    tA.setSourceCode(stage.getInjectedSource(c.getName(), mN, cmd));
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