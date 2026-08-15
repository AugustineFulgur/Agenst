package indi.augusttheodor.stage;

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
}