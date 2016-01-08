package humbleactivity.app;

import com.jakewharton.rxrelay.PublishRelay;
import rx.Observable;
import timber.log.Timber;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ChainComposer extends Presenter<ChainComposer.View> {
    private final EffectorService effectorService;
    private final RxScheduling rxScheduling;
    private final PublishRelay<Void> refreshRelay = PublishRelay.create();
    List<Filter> availableFilters;
    List<Filter> chain;

    @Inject
    public ChainComposer(EffectorService effectorService, RxScheduling rxScheduling) {
        this.effectorService = effectorService;
        this.rxScheduling = rxScheduling;
        Timber.d("Created");
    }

    public void attach(View view) {
        super.attach(view);
        subscriptions.add(refreshRelay.flatMap(none ->
                rxScheduling.httpCall(effectorService.listFilters())
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

    public interface View extends PassiveView {
        void setAvailableFilters(List<Filter> filters);

        void setChain(List<Filter> chain);

        void showErrorMessage(String message);
    }
}
