package humbleactivity.app.ui

import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.functions.Action1

class Counter(initial: Int) {
    private val up = PublishRelay.create<Unit>()
    private val down = PublishRelay.create<Unit>()
    private val count = up.map { 1 }.mergeWith(down.map { -1 })
            .scan(initial, { count, delta -> count + delta })

    fun count(): Observable<Int> = count

    fun onUp(): Action1<Unit> = up

    fun onDown(): Action1<Unit> = down
}