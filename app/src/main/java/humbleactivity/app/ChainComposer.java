package humbleactivity.app;

import rx.schedulers.Schedulers;

import java.util.Collections;

public class ChainComposer {
    private final ChainComposerView view;
    private final EffectorService effectorService;

    public ChainComposer(ChainComposerView view, EffectorService effectorService) {
        this.view = view;
        this.effectorService = effectorService;
    }

    public void initialize() {
        effectorService.listFilters()
                .subscribeOn(Schedulers.io())
                .subscribe(filters -> {
                    view.setAvailableFilters(filters);
                    view.setChain(Collections.<Filter>emptyList());
                }, throwable -> {
                    view.showErrorMessage(throwable.getMessage());
                });
    }
}
