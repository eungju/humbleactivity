package humbleactivity.app;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DataModule.class, RxModule.class})
public interface HumbleComponent {
    void inject(ChainComposerActivity chainComposerActivity);
}
