package humbleactivity.app.ui

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.Observable

class Counter(initial: Int) {
    val up: PublishRelay<Unit> = PublishRelay.create()
    val down: PublishRelay<Unit> = PublishRelay.create()

    val count: Observable<Int> = up.map { { count: Int -> count + 1 } }
            .mergeWith(down.map { { count: Int -> count - 1 } })
            .scan(initial) { state, action -> action(state) }
            .cacheWithInitialCapacity(1)
}
