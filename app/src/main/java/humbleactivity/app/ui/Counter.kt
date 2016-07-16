package humbleactivity.app.ui

import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.functions.Action1

class Counter(initial: Int) {
    data class State(val count: Int) {
        fun up(): State = copy(count = count + 1)

        fun down(): State = copy(count = count - 1)
    }

    private val up = PublishRelay.create<Unit>()
    private val down = PublishRelay.create<Unit>()
    private val state = up.map { { state: State -> state.up() } }
            .mergeWith(down.map { { state: State -> state.down() } })
            .scan(State(initial), { state, action -> action(state) })
            .cacheWithInitialCapacity(1)

    fun count(): Observable<Int> = state.map { it.count }

    fun onUp(): Action1<Unit> = up

    fun onDown(): Action1<Unit> = down
}