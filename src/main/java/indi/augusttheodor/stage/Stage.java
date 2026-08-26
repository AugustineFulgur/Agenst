package indi.augusttheodor.stage;

import indi.augusttheodor.Options;
import java.util.Map;
import java.util.Set;

public interface Stage {
    String getName();
    Map<String, String[]> getHookPoints();
    void configure(Options options);
    String getInjectedSource(String className, String methodName, Options options);
    default boolean isPresent(Set<String> loadedNames) {
        Set<String> markers = getMarkers();
        return markers.isEmpty() || !java.util.Collections.disjoint(markers, loadedNames);
    }
    default Set<String> getMarkers() { return java.util.Collections.emptySet(); }
}
