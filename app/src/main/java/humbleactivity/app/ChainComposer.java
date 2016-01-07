package humbleactivity.app;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChainComposer {
    private final ChainComposerView view;
    private final EffectorService effectorService;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;
    List<Filter> availableFilters;
    List<Filter> chain;

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
                    availableFilters = new ArrayList<>(filters);
                    chain = new ArrayList<>();
                    view.setAvailableFilters(filters);
                    view.setChain(Collections.<Filter>emptyList());
                }, throwable -> {
                    view.showErrorMessage(throwable.getMessage());
                });
    }

    public void addToChain(int selectedItemPosition) {
        if (move(availableFilters, chain, selectedItemPosition)) {
            view.setAvailableFilters(availableFilters);
            view.setChain(chain);
        }
    }

    public void removeFromChain(int selectedItemPosition) {
        if (move(chain, availableFilters, selectedItemPosition)) {
            view.setAvailableFilters(availableFilters);
            view.setChain(chain);
        }
    }

    static boolean move(List<Filter> from, List<Filter> to, int index) {
        if (index < 0 || index >= from.size()) {
            return false;
        }
        Filter selected = from.get(index);
        to.add(selected);
        from.remove(index);
        return true;
    }
}
