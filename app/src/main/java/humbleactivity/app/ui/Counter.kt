package humbleactivity.app.ui

import com.jakewharton.rxrelay.BehaviorRelay
import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.functions.Action1

class Counter {
    private val current = BehaviorRelay.create<Int>()
    private val up = PublishRelay.create<Unit>()
    private val down = PublishRelay.create<Unit>()
    private val currentUpdate = current.mergeWith(up.withLatestFrom(current, { a, b -> b + 1 })
            .mergeWith(down.withLatestFrom(current, { a, b -> b - 1 }))
            .doOnNext { current.call(it) }
            .ignoreElements())

    fun current(): Observable<Int> = currentUpdate

    fun onUp(): Action1<Unit> = up

    fun onDown(): Action1<Unit> = down

    fun initialize(initial: Int) {
        current.call(initial)
    }
}