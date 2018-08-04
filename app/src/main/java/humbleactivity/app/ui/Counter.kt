package humbleactivity.app.ui

import com.jakewharton.rxrelay2.PublishRelay

class Counter(initial: Int) {
    val up = PublishRelay.create<Unit>()
    val down = PublishRelay.create<Unit>()
    val count = up.map { { count: Int -> count + 1 } }
            .mergeWith(down.map { { count: Int -> count - 1 } })
            .scan(initial, { state, action -> action(state) })
            .cacheWithInitialCapacity(1)
}