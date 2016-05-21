package humbleactivity.app.data

import rx.Observable

internal class DummyEffectorService : EffectorService {
    override fun listFilters(): Observable<List<Filter>> {
        return Observable.just(listOf(Filter("Reverb"), Filter("Distortion"), Filter("Chorus"), Filter("Delay")))
    }
}
