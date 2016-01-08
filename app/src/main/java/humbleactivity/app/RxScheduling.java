package humbleactivity.app;

import rx.Observable;
import rx.Scheduler;

public class RxScheduling {
    public final Scheduler io;
    public final Scheduler ui;

    public RxScheduling(Scheduler io, Scheduler ui) {
        this.io = io;
        this.ui = ui;
    }

    public <T> Observable<T> ioThenUi(Observable<T> observable) {
        return observable.subscribeOn(io).observeOn(ui);
    }
}
