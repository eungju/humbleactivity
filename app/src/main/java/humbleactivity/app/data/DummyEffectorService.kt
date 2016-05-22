package humbleactivity.app.data

import rx.Observable
import java.util.concurrent.TimeUnit

internal class DummyEffectorService : EffectorService {
    override fun listFilters(): Observable<List<Filter>> {
        return Observable.just(listOf(Filter("Reverb"), Filter("Distortion"), Filter("Chorus"), Filter("Delay"))).delay(1, TimeUnit.SECONDS)
    }
}
