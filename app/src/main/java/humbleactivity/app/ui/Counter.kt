package humbleactivity.app.ui

import com.jakewharton.rxrelay.BehaviorRelay
import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.functions.Action1

class Counter {
    private val count = BehaviorRelay.create<Int>()
    private val up = PublishRelay.create<Unit>()
    private val down = PublishRelay.create<Unit>()
    private val stateUpdate = count.mergeWith(up.withLatestFrom(count, { up, count -> count + 1 })
            .mergeWith(down.withLatestFrom(count, { down, count -> count - 1 }))
            .doOnNext { count.call(it) }
            .ignoreElements())

    fun current(): Observable<Int> = stateUpdate

    fun onUp(): Action1<Unit> = up

    fun onDown(): Action1<Unit> = down

    fun initialize(initial: Int) {
        count.call(initial)
    }
}