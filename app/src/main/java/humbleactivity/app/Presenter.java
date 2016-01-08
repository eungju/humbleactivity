package humbleactivity.app;

import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public abstract class Presenter<ViewType extends PassiveView> {
    protected ViewType view;
    protected CompositeSubscription subscriptions;

    public void attach(ViewType view) {
        Timber.d("Attaching %s", view.toString());
        this.view = view;
        this.subscriptions = new CompositeSubscription();
    }

    public void detach() {
        Timber.d("Detaching %s", view.toString());
        subscriptions.unsubscribe();
        view = null;
    }
}
