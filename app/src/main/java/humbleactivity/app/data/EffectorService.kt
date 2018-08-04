package humbleactivity.app.data

import io.reactivex.Observable
import retrofit2.http.GET

interface EffectorService {
    @GET("filters")
    fun listFilters(): Observable<List<Filter>>
}
