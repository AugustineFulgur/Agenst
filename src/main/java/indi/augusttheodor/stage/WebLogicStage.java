package indi.augusttheodor.stage;

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
}