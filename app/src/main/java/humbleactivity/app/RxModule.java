package humbleactivity.app;

import dagger.Module;
import dagger.Provides;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import javax.inject.Singleton;

@Module
public class RxModule {
    @Singleton
    @Provides
    public RxScheduling rxScheduling() {
        return new RxScheduling(Schedulers.io(), AndroidSchedulers.mainThread());
    }
}
