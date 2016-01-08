package humbleactivity.app;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class DataModule {
    @Singleton
    @Provides
    public EffectorService effectorService() {
        return new DummyEffectorService();
    }
}
