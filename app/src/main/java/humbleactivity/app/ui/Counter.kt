package humbleactivity.app.ui

import com.jakewharton.rxrelay.BehaviorRelay
import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.functions.Action1

class Counter {
    data class State(val count: Int) {
        fun up(): State { return copy(count = count + 1) }
        fun down(): State { return copy(count = count - 1) }
    }
    private val state = BehaviorRelay.create<State>()
    private val up = PublishRelay.create<Unit>()
    private val down = PublishRelay.create<Unit>()
    private val stateUpdate = state
            .mergeWith(up.withLatestFrom(state, { up, state -> state.up() })
                    .mergeWith(down.withLatestFrom(state, { down, state -> state.down() }))
                    .doOnNext(state)
                    .ignoreElements())

    fun current(): Observable<Int> = stateUpdate.map { it.count }

    fun onUp(): Action1<Unit> = up

    fun onDown(): Action1<Unit> = down

    fun initialize(initial: Int) {
        state.call(State(initial))
    }
}