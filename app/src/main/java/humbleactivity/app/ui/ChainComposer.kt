package humbleactivity.app.ui

import com.jakewharton.rxrelay.BehaviorRelay
import com.jakewharton.rxrelay.PublishRelay
import humbleactivity.app.RxScheduling
import humbleactivity.app.data.EffectorService
import humbleactivity.app.data.Filter
import humbleactivity.app.data.removeAt
import humbleactivity.app.data.swap
import rx.Observable
import rx.functions.Action1

import javax.inject.Inject

class ChainComposer
@Inject
constructor(effectorService: EffectorService,
            rxScheduling: RxScheduling) {
    val availables = BehaviorRelay.create<List<Filter>>()
    val chain = BehaviorRelay.create<List<Filter>>()
    private val refresh = PublishRelay.create<Unit>()
    private val addToChain = PublishRelay.create<Int>()
    private val removeFromChain = PublishRelay.create<Int>()
    private val moveUp = PublishRelay.create<Int>()
    private val moveDown = PublishRelay.create<Int>()
    private val loadError = PublishRelay.create<String>()
    private val loadAvailables = refresh
            .concatMap {
                rxScheduling.subscribeOnIoObserveOnUi(effectorService.listFilters())
                        .doOnError { throwable -> loadError.call(throwable.message!!) }
                        .onErrorResumeNext(Observable.empty())
            }
    private val chainCursorMove = PublishRelay.create<Int>()
    private val crossUpdate = loadAvailables.map { a -> Pair(a, emptyList<Filter>()) }
            .mergeWith(addToChain.withLatestFrom(Observable.combineLatest(availables, chain, { a, b -> Pair(a, b) }), { position, a_b ->
                val (from, to) = a_b
                Pair(from.removeAt(position), to + from[position])
            }))
            .mergeWith(removeFromChain.withLatestFrom(Observable.combineLatest(availables, chain, { a, b -> Pair(a, b) }), { position, a_b ->
                val (to, from) = a_b
                Pair(to + from[position], from.removeAt(position))
            }))
            .subscribe { a_b -> availables.call(a_b.first); chain.call(a_b.second) }
    private val chainUpdate = moveDown.withLatestFrom(chain, { position, b -> Pair(b.swap(position, position + 1), position + 1) })
            .mergeWith(moveUp.withLatestFrom(chain, { position, b -> Pair(b.swap(position, position - 1), position - 1) }))
            .subscribe { b ->
                chain.call(b.first)
                chainCursorMove.call(b.second)
            }

    fun availables(): Observable<List<Filter>> = availables

    fun chain(): Observable<List<Filter>> = chain

    fun chainCursorMove(): Observable<Int> = chainCursorMove

    fun loadError(): Observable<String> = loadError

    fun onRefresh(): Action1<Unit> = refresh

    fun onAddToChain(): Action1<Int> = addToChain

    fun onRemoveFromChain(): Action1<Int> = removeFromChain

    fun onMoveUp(): Action1<Int> = moveUp

    fun onMoveDown(): Action1<Int> = moveDown

    fun initialize() {
        availables.call(emptyList())
        chain.call(emptyList())
        refresh.call(Unit)
    }

    fun dispose() {
        crossUpdate.unsubscribe()
        chainUpdate.unsubscribe()
    }
}
