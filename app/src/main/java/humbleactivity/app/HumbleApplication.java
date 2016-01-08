package humbleactivity.app;

import android.app.Application;
import android.content.Context;
import timber.log.Timber;

public class HumbleApplication extends Application {
    private HumbleComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new Timber.DebugTree());

        component = DaggerHumbleComponent.builder().build();
    }

    public HumbleComponent component() {
        return component;
    }

    public static HumbleApplication get(Context context) {
        return (HumbleApplication) context.getApplicationContext();
    }
}
