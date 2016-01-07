package humbleactivity.app;

import rx.Observable;

import java.util.Arrays;
import java.util.List;

class DummyEffectorService implements EffectorService {
    @Override
    public Observable<List<Filter>> listFilters() {
        return Observable.just(Arrays.asList(new Filter("Reverb"), new Filter("Distortion"), new Filter("Chorus"), new Filter("Delay")));
    }
}
