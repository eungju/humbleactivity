package humbleactivity.app;

import rx.subscriptions.CompositeSubscription;

public abstract class Presenter<ViewType extends PassiveView> {
    protected final ViewType view;
    protected final CompositeSubscription subscriptions;

    public Presenter(ViewType view) {
        this.view = view;
        subscriptions = new CompositeSubscription();
    }

    public void destroy() {
        subscriptions.unsubscribe();
    }
}
