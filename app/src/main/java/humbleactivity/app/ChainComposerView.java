package humbleactivity.app;

import java.util.List;

public interface ChainComposerView {
    void setAvailableFilters(List<Filter> filters);

    void setChain(List<Filter> chain);
}
