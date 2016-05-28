package humbleactivity.app.ui

import com.jakewharton.rxrelay.BehaviorRelay
import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.functions.Action1

class Counter {
    private val state = BehaviorRelay.create<Int>()
    private val up = PublishRelay.create<Unit>()
    private val down = PublishRelay.create<Unit>()
    private val stateUpdate = state
            .mergeWith(up.withLatestFrom(state, { up, state -> state + 1 })
                    .mergeWith(down.withLatestFrom(state, { down, state -> state - 1 }))
                    .doOnNext(state)
                    .ignoreElements())

    fun current(): Observable<Int> = stateUpdate

    fun onUp(): Action1<Unit> = up

    fun onDown(): Action1<Unit> = down

    fun initialize(initial: Int) {
        state.call(initial)
    }
}