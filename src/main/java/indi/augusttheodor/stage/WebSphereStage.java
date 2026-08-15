package indi.augusttheodor.stage;

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
}