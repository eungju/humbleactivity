package humbleactivity.app;

import io.reactivex.Scheduler;

public class RxScheduling {
    public final Scheduler io;
    public final Scheduler ui;

    public RxScheduling(Scheduler io, Scheduler ui) {
        this.io = io;
        this.ui = ui;
    }
}
