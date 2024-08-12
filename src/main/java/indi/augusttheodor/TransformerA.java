package indi.augusttheodor;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Objects;

import javassist.*;

//自定义Transformer，覆写sourceCode到declaredMethod
public class TransformerA implements ClassFileTransformer {
    public String declaredMethod="";
    public String className="";
    public String sourceCode="";
    public static ClassPool cp=null;

    public static void init(){
        //初始化
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
        //只能说并不完全适用
        if(TransformerA.cp==null){
            TransformerA.init();
        }
        if(Objects.equals(this.declaredMethod, "") || Objects.equals(this.className, "") || Objects.equals(this.sourceCode, "")){
            return null;
        }//先检查是否初始化等
        if(classBeingRedefined!=null){
            //有重定义也插进来
            TransformerA.cp.insertClassPath(new ClassClassPath(classBeingRedefined));
        }
        try {
            System.out.println(this.sourceCode+this.className+this.declaredMethod);
            CtClass cc=TransformerA.cp.get(this.className);
            CtMethod cm=cc.getDeclaredMethod(this.declaredMethod);
            cm.insertBefore(this.sourceCode); //插入原始代码
            byte[] b=cc.toBytecode();
            cc.detach();
            return b;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getBytes(Class redefined){
        //只能说并不完全适用
        if(TransformerA.cp==null){
            TransformerA.init();
        }
        TransformerA.cp.insertClassPath(new ClassClassPath(redefined));
        if(Objects.equals(this.declaredMethod, "") || Objects.equals(this.className, "") || Objects.equals(this.sourceCode, "")){
            return null;
        }//先检查是否初始化等
        try {
            System.out.println(this.sourceCode+this.className+this.declaredMethod);
            CtClass cc=TransformerA.cp.get(this.className);
            CtMethod cm=cc.getDeclaredMethod(this.declaredMethod);
            cm.insertBefore(this.sourceCode); //插入原始代码
            byte[] b=cc.toBytecode();
            cc.detach();
            return b;
        } catch (NotFoundException | CannotCompileException | IOException e) {
            throw new RuntimeException(e);
        }
    }

}
