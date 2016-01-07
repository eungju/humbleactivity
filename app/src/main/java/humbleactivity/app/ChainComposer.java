package humbleactivity.app;

import java.util.Arrays;
import java.util.Collections;

public class ChainComposer {
    private final ChainComposerView view;

    public ChainComposer(ChainComposerView view) {
        this.view = view;
    }

    public void initialize() {
        view.setAvailableFilters(Arrays.asList(new Filter("A")));
        view.setChain(Collections.<Filter>emptyList());
    }
}
