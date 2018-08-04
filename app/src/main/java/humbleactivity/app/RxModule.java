package humbleactivity.app;

import dagger.Module;
import dagger.Provides;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Singleton;

@Module
public class RxModule {
    @Singleton
    @Provides
    public RxScheduling rxScheduling() {
        return new RxScheduling(Schedulers.io(), AndroidSchedulers.mainThread());
    }
}
