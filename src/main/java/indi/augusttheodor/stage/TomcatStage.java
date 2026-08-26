package indi.augusttheodor.stage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TomcatStage extends ServletFilterChainStage {
    @Override
    public String getName() {
        return "tomcat";
    }
    @Override
    protected String getClassName() {
        return "org.apache.catalina.core.ApplicationFilterChain";
    }
    @Override
    protected String getMethodName() {
        return "doFilter";
    }
    @Override
    public Set<String> getMarkers() {
        return new HashSet<>(Arrays.asList(
                "org.apache.catalina.startup.Bootstrap",
                "org.apache.catalina.core.StandardContext"
        ));
    }
}
