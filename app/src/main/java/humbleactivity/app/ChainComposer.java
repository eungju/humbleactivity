package humbleactivity.app;

import com.jakewharton.rxrelay.PublishRelay;
import rx.Observable;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import java.util.ArrayList;
import java.util.List;

public class ChainComposer {
    private final ChainComposerView view;
    private final EffectorService effectorService;
    private final Scheduler ioScheduler;
    private final Scheduler uiScheduler;
    private final CompositeSubscription subscriptions = new CompositeSubscription();
    private final PublishRelay<Void> refreshRelay = PublishRelay.create();
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
        subscriptions.add(refreshRelay.flatMap(none ->
                effectorService.listFilters()
                        .subscribeOn(ioScheduler)
                        .observeOn(uiScheduler)
                        .doOnError(throwable -> {
                            view.showErrorMessage(throwable.getMessage());
                        })
                        .onErrorResumeNext(Observable.empty())
        ).subscribe(filters -> {
            availableFilters = new ArrayList<>(filters);
            chain = new ArrayList<>();
            view.setAvailableFilters(availableFilters);
            view.setChain(chain);
        }));
    }

    public void initialize() {
        refreshRelay.call(null);
    }

    public void refresh() {
        refreshRelay.call(null);
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

    public void destroy() {
        subscriptions.unsubscribe();
    }
}
