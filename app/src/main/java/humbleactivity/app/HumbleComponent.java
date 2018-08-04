package humbleactivity.app;

import dagger.Component;
import humbleactivity.app.data.DataModule;
import humbleactivity.app.ui.ChainComposerActivity;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DataModule.class})
public interface HumbleComponent {
    void inject(ChainComposerActivity chainComposerActivity);
}
