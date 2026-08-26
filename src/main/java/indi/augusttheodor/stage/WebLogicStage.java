package indi.augusttheodor.stage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WebLogicStage extends ServletFilterChainStage {
    @Override
    public String getName() {
        return "weblogic";
    }
    @Override
    protected String getClassName() {
        return "weblogic.servlet.internal.FilterChainImpl";
    }
    @Override
    protected String getMethodName() {
        return "doFilter";
    }
    @Override
    public Set<String> getMarkers() {
        return new HashSet<>(Arrays.asList(
                "weblogic.servlet.internal.WebAppServletContext",
                "weblogic.Server"
        ));
    }
}
