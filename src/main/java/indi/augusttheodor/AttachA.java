package indi.augusttheodor;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import indi.augusttheodor.stage.JasyptStage;
import indi.augusttheodor.stage.JettyStage;
import indi.augusttheodor.stage.Stage;
import indi.augusttheodor.stage.TomcatStage;
import indi.augusttheodor.stage.UndertowStage;
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
        STAGES.add(new UndertowStage());
        STAGES.add(new JasyptStage());
    }

    public static List<Stage> getStages() {
        return STAGES;
    }

    public static void doAttach(String args) throws IOException, AgentLoadException, AgentInitializationException {
        String jarPath = AttachA.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = new File(jarPath).getPath();
        List<VirtualMachineDescriptor> vList;
        try {
            vList = VirtualMachine.list();
        } catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
            System.out.println("[错误] 无法加载 Java Attach 机制，通常是因为当前运行环境是 JRE 而非 JDK，");
            System.out.println("        或缺少对应平台的 attach native 库（Windows: attach.dll / Linux: libattach.so）。");
            System.out.println("        请使用 JDK 运行 Agenst，或将 attach 的 native 库放到 JRE 的 bin/lib 目录。");
            return;
        } catch (Exception e) {
            System.out.println("[错误] 枚举 JVM 列表失败：" + e.getMessage());
            return;
        }

        if (vList == null || vList.isEmpty()) {
            System.out.println("[提示] 未找到可附加的 JVM 进程。");
            return;
        }

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
            } catch (Exception e) {
                System.out.println(vmdName + " ---- 不支持 Attach(" + e + ")！注意：这通常提示当前进程已被另一个 Agent 接管（可能是 RASP）");
            }
        }
    }
}
