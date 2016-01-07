package humbleactivity.app;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.Collections;

public class ChainComposer {
    private final ChainComposerView view;
    private final EffectorService effectorService;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;

    public ChainComposer(ChainComposerView view) {
        this(view, new DummyEffectorService(), Schedulers.io(), AndroidSchedulers.mainThread());
    }

    public ChainComposer(ChainComposerView view, EffectorService effectorService, Scheduler ioScheduler, Scheduler uiScheduler) {
        this.view = view;
        this.effectorService = effectorService;
        this.ioScheduler = ioScheduler;
        this.uiScheduler = uiScheduler;
    }

    public void initialize() {
        effectorService.listFilters()
                .subscribeOn(ioScheduler)
                .observeOn(uiScheduler)
                .subscribe(filters -> {
                    view.setAvailableFilters(filters);
                    view.setChain(Collections.<Filter>emptyList());
                }, throwable -> {
                    view.showErrorMessage(throwable.getMessage());
                });
    }
}
