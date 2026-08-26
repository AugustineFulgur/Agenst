package indi.augusttheodor;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class OneShotTransformer implements ClassFileTransformer {

    public static final class Target {
        public final String[] methods;
        public final String source;

        public Target(String[] methods, String source) {
            this.methods = methods;
            this.source = source;
        }
    }

    private static volatile Instrumentation INST;
    private final ConcurrentHashMap<String, Target> pending;
    private final AtomicBoolean removed = new AtomicBoolean(false);

    public static void bind(Instrumentation ins) {
        INST = ins;
    }

    public OneShotTransformer(Map<String, Target> targets) {
        this.pending = new ConcurrentHashMap<>(targets);
    }

    public boolean isSettled() {
        return pending.isEmpty();
    }

    public void unregister() {
        if (removed.compareAndSet(false, true) && INST != null) {
            INST.removeTransformer(this);
        }
    }

    public void armReaper(long ms) {
        Thread r = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(ms);
                } catch (InterruptedException ignored) {
                }
                unregister();
            }
        }, "agenst-reaper");
        r.setDaemon(true);
        r.start();
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        Target t = pending.remove(className == null ? "" : className.replace('/', '.'));
        if (t == null) {
            return null;
        }
        try {
            byte[] out = inject(loader, t, classfileBuffer);
            if (pending.isEmpty()) {
                unregister();
            }
            return out;
        } catch (Throwable e) {
            return null;
        }
    }

    private byte[] inject(ClassLoader loader, Target t, byte[] buffer) throws Exception {
        ClassPool cp = new ClassPool(false);
        cp.appendClassPath(new LoaderClassPath(loader));
        cp.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        CtClass cc = cp.makeClass(new ByteArrayInputStream(buffer));
        try {
            for (String m : t.methods) {
                cc.getDeclaredMethod(m).insertBefore(t.source);
            }
            return cc.toBytecode();
        } finally {
            cc.detach();
        }
    }
}
