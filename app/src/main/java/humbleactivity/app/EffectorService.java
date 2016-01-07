package humbleactivity.app;

import retrofit2.http.GET;
import rx.Observable;

import java.util.List;

public interface EffectorService {
    @GET("filters")
    Observable<List<Filter>> listFilters();
}
