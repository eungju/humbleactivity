package humbleactivity.app;

import rx.functions.Action1;

import java.util.Collections;
import java.util.List;

public class ChainComposer {
    private final ChainComposerView view;
    private final EffectorService effectorService;

    public ChainComposer(ChainComposerView view, EffectorService effectorService) {
        this.view = view;
        this.effectorService = effectorService;
    }

    public void initialize() {
        effectorService.listFilters().subscribe(new Action1<List<Filter>>() {
            @Override
            public void call(List<Filter> filters) {
                view.setAvailableFilters(filters);
                view.setChain(Collections.<Filter>emptyList());
            }
        });
    }
}
