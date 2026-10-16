package indi.augusttheodor.stage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WebSphereStage extends ServletFilterChainStage {
    @Override
    public String getName() {
        return "websphere";
    }
    @Override
    protected String getClassName() {
        return "com.ibm.ws.webcontainer.filter.WebAppFilterChain";
    }
    @Override
    protected String getMethodName() {
        return "doFilter";
    }
    @Override
    public Set<String> getMarkers() {
        return new HashSet<>(Arrays.asList(
                "com.ibm.ws.webcontainer.filter.WebAppFilterChain",
                "com.ibm.ws.runtime.WsServer"
        ));
    }
}
