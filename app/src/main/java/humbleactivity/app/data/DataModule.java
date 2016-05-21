package humbleactivity.app.data;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DataModule {
    @Singleton
    @Provides
    public EffectorService effectorService() {
        return new DummyEffectorService();
    }
}
