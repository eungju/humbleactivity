package humbleactivity.app.ui;

import com.jakewharton.rxrelay.PublishRelay;
import humbleactivity.app.RxScheduling;
import humbleactivity.app.data.EffectorService;
import humbleactivity.app.data.Filter;
import rx.Observable;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ChainComposer extends Presenter<ChainComposer.ChainComposerView> {
    private final EffectorService effectorService;
    private final RxScheduling rxScheduling;
    private final PublishRelay<Void> refreshRelay = PublishRelay.create();
    public List<Filter> availableFilters;
    public List<Filter> chain;

    @Inject
    public ChainComposer(EffectorService effectorService, RxScheduling rxScheduling) {
        this.effectorService = effectorService;
        this.rxScheduling = rxScheduling;
        Timber.d("Created");
    }

    public void attach(ChainComposerView view) {
        super.attach(view);
        subscriptions.add(refreshRelay.flatMap(none ->
                rxScheduling.ioThenUi(effectorService.listFilters())
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

    public void moveUpFilter(int index) {
        if (index < 1 || index >= chain.size()) {
            return;
        }
        Filter picked = chain.remove(index);
        chain.add(index - 1, picked);
        view.swapFilterInChain(index, index - 1);
    }

    public void moveDownFilter(int index) {
        if (index < 0 || index >= chain.size() - 1) {
            return;
        }
        Filter picked = chain.remove(index);
        chain.add(index + 1, picked);
        view.swapFilterInChain(index, index + 1);
    }

    public interface ChainComposerView extends PassiveView {
        void setAvailableFilters(List<Filter> filters);

        void setChain(List<Filter> chain);

        void showErrorMessage(String message);

        void swapFilterInChain(int from, int to);
    }
}
