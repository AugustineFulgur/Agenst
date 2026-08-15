package indi.augusttheodor.stage;

import indi.augusttheodor.Options;
import java.util.Map;

public interface Stage {
    String getName();
    Map<String, String[]> getHookPoints();
    void configure(Options options);
    String getInjectedSource(String className, String methodName, Options options);
}