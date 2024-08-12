package indi.augusttheodor;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

import javassist.*;

//�Զ���Transformer����дsourceCode��declaredMethod
public class TransformerA implements ClassFileTransformer {
    public String declaredMethod="";
    public String className="";
    public String sourceCode="";
    public static ClassPool cp=null;

    public static void init(){
        //��ʼ��
        TransformerA.cp=ClassPool.getDefault();
        TransformerA.cp.appendClassPath(new LoaderClassPath(Thread.currentThread().getContextClassLoader()));
    }

    public TransformerA(String declaredMethod,String className){
        this.declaredMethod=declaredMethod;
        this.className=className;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        //ֻ��˵������ȫ����
        if(TransformerA.cp==null){
            TransformerA.init();
        }
        if(Objects.equals(this.declaredMethod, "") || Objects.equals(this.className, "") || Objects.equals(this.sourceCode, "")){
            return null;
        }//�ȼ���Ƿ��ʼ����
        if(classBeingRedefined!=null){
            //���ض���Ҳ�����
            TransformerA.cp.insertClassPath(new ClassClassPath(classBeingRedefined));
        }
        try {
            System.out.println(this.sourceCode+this.className+this.declaredMethod);
            CtClass cc=TransformerA.cp.get(this.className);
            CtMethod cm=cc.getDeclaredMethod(this.declaredMethod);
            cm.insertBefore(this.sourceCode); //����ԭʼ����
            byte[] b=cc.toBytecode();
            cc.detach();
            return b;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getBytes(Class redefined){
        //ֻ��˵������ȫ����
        if(TransformerA.cp==null){
            TransformerA.init();
        }
        TransformerA.cp.insertClassPath(new ClassClassPath(redefined));
        if(Objects.equals(this.declaredMethod, "") || Objects.equals(this.className, "") || Objects.equals(this.sourceCode, "")){
            return null;
        }//�ȼ���Ƿ��ʼ����
        try {
            System.out.println(this.sourceCode+this.className+this.declaredMethod);
            CtClass cc=TransformerA.cp.get(this.className);
            CtMethod cm=cc.getDeclaredMethod(this.declaredMethod);
            cm.insertBefore(this.sourceCode); //����ԭʼ����
            byte[] b=cc.toBytecode();
            cc.detach();
            return b;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
