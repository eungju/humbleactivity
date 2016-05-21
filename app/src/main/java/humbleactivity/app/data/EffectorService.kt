package humbleactivity.app.data

import retrofit2.http.GET
import rx.Observable

interface EffectorService {
    @GET("filters")
    fun listFilters(): Observable<List<Filter>>
}
