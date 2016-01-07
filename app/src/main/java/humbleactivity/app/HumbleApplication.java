package humbleactivity.app;

import android.app.Application;
import timber.log.Timber;

public class HumbleApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());
    }
}
