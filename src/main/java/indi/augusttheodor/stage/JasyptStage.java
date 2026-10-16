package indi.augusttheodor.stage;

import indi.augusttheodor.Options;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JasyptStage implements Stage {

    private static final AtomicBoolean TRIGGERED = new AtomicBoolean(false);
    private static final Pattern ENCRYPTED_PATTERN = Pattern.compile("^ENC\\((.+)\\)$");

    @Override
    public String getName() {
        return "jasypt";
    }

    @Override
    public Map<String, String[]> getHookPoints() {
        Map<String, String[]> re = new HashMap<>();
        re.put("org.springframework.core.env.AbstractEnvironment", new String[]{"getPropertySources"});
        return re;
    }

    @Override
    public void configure(Options options) {
        // no extra options needed
    }

    @Override
    public Set<String> getMarkers() {
        return new HashSet<>(Arrays.asList("org.springframework.core.env.AbstractEnvironment"));
    }

    @Override
    public String getInjectedSource(String className, String methodName, Options options) {
        return "{ indi.augusttheodor.stage.JasyptStage.dumpEncryptedProperties(this, this.getClass().getClassLoader()); }";
    }

    public static void dumpEncryptedProperties(Object env, ClassLoader loader) {
        if (!TRIGGERED.compareAndSet(false, true)) {
            return;
        }
        if (env == null || loader == null) {
            return;
        }
        Object encryptor = null;
        String err = null;
        try {
            encryptor = buildEncryptor(loader);
        } catch (Throwable t) {
            err = t.getMessage();
        }
        if (encryptor == null) {
            System.out.println("[JasyptStage] 未能构造解密器" + (err != null ? "：" + err : "，将只打印 ENC(...) 密文"));
        }
        try {
            Class<?> abstractEnvClass = Class.forName("org.springframework.core.env.AbstractEnvironment", true, loader);
            java.lang.reflect.Field field = abstractEnvClass.getDeclaredField("propertySources");
            field.setAccessible(true);
            Object propertySources = field.get(env);
            if (propertySources == null) {
                return;
            }
            Iterator<?> it = (Iterator<?>) propertySources.getClass().getMethod("iterator").invoke(propertySources);
            while (it.hasNext()) {
                Object source = it.next();
                if (source == null || !isEnumerable(source)) {
                    continue;
                }
                String sourceName = getSourceName(source);
                String[] names = (String[]) source.getClass().getMethod("getPropertyNames").invoke(source);
                if (names == null) {
                    continue;
                }
                for (String name : names) {
                    Object value = source.getClass().getMethod("getProperty", String.class).invoke(source, name);
                    if (value == null) {
                        continue;
                    }
                    String s = value.toString();
                    Matcher matcher = ENCRYPTED_PATTERN.matcher(s);
                    if (matcher.matches()) {
                        String cipher = matcher.group(1);
                        String plain = null;
                        if (encryptor != null) {
                            try {
                                plain = decrypt(encryptor, cipher);
                            } catch (Throwable t) {
                                plain = "<解密失败：" + t.getMessage() + ">";
                            }
                        }
                        if (plain != null) {
                            System.out.println("[JasyptStage] " + sourceName + " / " + name + " = " + plain + "  (密文：" + s + ")");
                        } else {
                            System.out.println("[JasyptStage] " + sourceName + " / " + name + " = " + s);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static Object buildEncryptor(ClassLoader loader) {
        String password = findPassword();
        if (password == null) {
            return null;
        }
        String algorithm = resolveAlgorithm();
        // 优先尝试 StandardPBEStringEncryptor，旧版 Jasypt 常见
        try {
            return createEncryptor(loader, password, algorithm,
                    "org.jasypt.encryption.pbe.StandardPBEStringEncryptor");
        } catch (Throwable ignored) {
        }
        // 新版或池化实现
        try {
            return createEncryptor(loader, password, algorithm,
                    "org.jasypt.encryption.pbe.PooledPBEStringEncryptor");
        } catch (Throwable ignored) {
        }
        return null;
    }

    private static Object createEncryptor(ClassLoader loader, String password, String algorithm, String className) throws Exception {
        Class<?> clazz = Class.forName(className, true, loader);
        Object encryptor = clazz.getDeclaredConstructor().newInstance();
        clazz.getMethod("setPassword", String.class).invoke(encryptor, password);
        if (algorithm != null && !algorithm.isEmpty()) {
            clazz.getMethod("setAlgorithm", String.class).invoke(encryptor, algorithm);
        }
        return encryptor;
    }

    private static String resolveAlgorithm() {
        String algorithm = System.getProperty("jasypt.encryptor.algorithm");
        if (algorithm != null && !algorithm.isEmpty()) {
            return algorithm;
        }
        algorithm = System.getenv("JASYPT_ENCRYPTOR_ALGORITHM");
        if (algorithm != null && !algorithm.isEmpty()) {
            return algorithm;
        }
        // Jasypt 3.x 默认算法；若失败会在上层尝试 PBEWithMD5AndDES 等旧版默认算法
        return "PBEWITHHMACSHA512ANDAES_256";
    }

    private static String findPassword() {
        String password = System.getProperty("jasypt.encryptor.password");
        if (password != null && !password.isEmpty()) {
            return password;
        }
        password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (password != null && !password.isEmpty()) {
            return password;
        }
        // 某些启动脚本会把密码写到 System Property 的变量名里
        password = System.getProperty("JASYPT_ENCRYPTOR_PASSWORD");
        if (password != null && !password.isEmpty()) {
            return password;
        }
        return null;
    }

    private static String decrypt(Object encryptor, String cipher) throws Exception {
        Class<?> clazz = encryptor.getClass();
        Object result = clazz.getMethod("decrypt", String.class).invoke(encryptor, cipher);
        return result == null ? null : result.toString();
    }

    private static boolean isEnumerable(Object source) {
        for (Class<?> c = source.getClass(); c != null; c = c.getSuperclass()) {
            if ("org.springframework.core.env.EnumerablePropertySource".equals(c.getName())) {
                return true;
            }
        }
        return false;
    }

    private static String getSourceName(Object source) {
        try {
            Object r = source.getClass().getMethod("getName").invoke(source);
            return r == null ? "?" : r.toString();
        } catch (Throwable t) {
            return "?";
        }
    }
}
