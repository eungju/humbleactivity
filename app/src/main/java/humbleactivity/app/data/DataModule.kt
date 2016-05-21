package humbleactivity.app.data

import dagger.Module
import dagger.Provides

import javax.inject.Singleton

@Module
class DataModule {
    @Singleton
    @Provides
    fun effectorService(): EffectorService {
        return DummyEffectorService()
    }
}
