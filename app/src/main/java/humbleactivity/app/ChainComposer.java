package humbleactivity.app;

import java.util.Collections;

public class ChainComposer {
    private final ChainComposerView view;
    private final EffectorService effectorService;

    public ChainComposer(ChainComposerView view, EffectorService effectorService) {
        this.view = view;
        this.effectorService = effectorService;
    }

    public void initialize() {
        effectorService.listFilters().subscribe(filters -> {
            view.setAvailableFilters(filters);
            view.setChain(Collections.<Filter>emptyList());
        });
    }
}
