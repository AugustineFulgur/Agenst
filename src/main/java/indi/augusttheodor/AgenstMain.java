package indi.augusttheodor;

import indi.augusttheodor.stage.Stage;
import org.apache.catalina.core.ExtendServices;
import org.apache.catalina.core.NeoServices;
import org.apache.catalina.core.RebeyondServices;

import java.lang.instrument.Instrumentation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AgenstMain {

    public static void main(String[] args) {
        try {
            String edit = "";
            for (String a : args) {
                edit += a;
                edit += "\n";
            }
            AttachA.doAttach(edit);
        } catch (Exception e) {
            System.out.println("[错误] Agenst 运行失败：" + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void agentmain(String agentArgs, Instrumentation ins) throws Exception {
        Options cmd = new Options(agentArgs);
        applyUrlOptions(cmd);
        for (Stage stage : AttachA.getStages()) {
            try {
                stage.configure(cmd);
            } catch (Throwable e) {
                System.out.println("[agenst] " + stage.getName() + " 配置失败: " + e);
            }
        }

        System.out.println("[agenst] target jdk = " + System.getProperty("java.version"));

        Set<String> loaded = new HashSet<>();
        for (Class<?> c : ins.getAllLoadedClasses()) {
            loaded.add(c.getName());
        }

        Map<String, OneShotTransformer.Target> targets = new HashMap<>();
        for (Stage stage : AttachA.getStages()) {
            try {
                if (!stage.isPresent(loaded)) {
                    continue;
                }
                for (Map.Entry<String, String[]> e : stage.getHookPoints().entrySet()) {
                    targets.put(e.getKey(), new OneShotTransformer.Target(
                            e.getValue(), stage.getInjectedSource(e.getKey(), e.getValue()[0], cmd)));
                }
            } catch (Throwable e) {
                System.out.println("[agenst] " + stage.getName() + " 准备失败: " + e);
            }
        }

        if (targets.isEmpty()) {
            System.out.println("[agenst] 未探测到受支持中间件，退出");
            return;
        }

        for (String name : targets.keySet()) {
            System.out.println("[agenst] target class = " + name);
        }

        OneShotTransformer.bind(ins);
        OneShotTransformer t = new OneShotTransformer(targets);
        ins.addTransformer(t, true);

        for (Class<?> c : ins.getAllLoadedClasses()) {
            if (targets.containsKey(c.getName())) {
                try {
                    ins.retransformClasses(c);
                    System.out.println("[agenst] injected = " + c.getName());
                } catch (Throwable e) {
                    System.out.println("[agenst] " + c.getName() + " 注入失败: " + e);
                }
            }
        }

        if (t.isSettled()) {
            t.unregister();
        } else {
            t.armReaper(60_000);
        }
    }

    private static void applyUrlOptions(Options cmd) {
        if (cmd.hasOption("u")) {
            ExtendServices.SERVICE_URL = cmd.getOptionValue("u") + "E";
            RebeyondServices.SERVICE_URL = cmd.getOptionValue("u") + "R";
            NeoServices.SERVICE_URL = cmd.getOptionValue("u") + "N";
        }
        if (cmd.hasOption("uR")) {
            RebeyondServices.SERVICE_URL = cmd.getOptionValue("uR");
        }
        if (cmd.hasOption("uN")) {
            NeoServices.SERVICE_URL = cmd.getOptionValue("uN");
        }
        if (cmd.hasOption("uE")) {
            ExtendServices.SERVICE_URL = cmd.getOptionValue("uE");
        }
    }
}
