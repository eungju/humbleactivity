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
    data class State(val availables: List<Filter>, val chain: List<Filter>)

    internal val state = BehaviorRelay.create<State>()
    private val chainCursor = PublishRelay.create<Int>()
    private val refresh = PublishRelay.create<Unit>()
    private val addToChain = PublishRelay.create<Int>()
    private val removeFromChain = PublishRelay.create<Int>()
    private val moveUp = PublishRelay.create<Int>()
    private val moveDown = PublishRelay.create<Int>()
    private val loadError = PublishRelay.create<String>()
    private val loadAvailables = refresh
            .concatMap {
                effectorService.listFilters()
                        .observeOn(rxScheduling.ui)
                        .doOnError { throwable -> loadError.call(throwable.message!!) }
                        .onErrorResumeNext(Observable.empty())
            }
    private val stateUpdate = state
            .mergeWith(loadAvailables.map { availables -> State(availables, emptyList()) }
                    .mergeWith(addToChain.withLatestFrom(state, { position, state ->
                        State(state.availables.removeAt(position), state.chain + state.availables[position])
                    }))
                    .mergeWith(removeFromChain.withLatestFrom(state, { position, state ->
                        State(state.availables + state.chain[position], state.chain.removeAt(position))
                    }))
                    .mergeWith(moveDown.withLatestFrom(state, { position, state ->
                        chainCursor.call(position + 1)
                        State(state.availables, state.chain.swap(position, position + 1)) }))
                    .mergeWith(moveUp.withLatestFrom(state, { position, state ->
                        chainCursor.call(position - 1)
                        State(state.availables, state.chain.swap(position, position - 1)) }))
                    .doOnNext(state)
                    .ignoreElements())
            .share()

    fun availables(): Observable<List<Filter>> = stateUpdate.map { it.availables }

    fun chain(): Observable<List<Filter>> = stateUpdate.map { it.chain }

    fun chainCursor(): Observable<Int> = chainCursor

    fun loadError(): Observable<String> = loadError

    fun onRefresh(): Action1<Unit> = refresh

    fun onAddToChain(): Action1<Int> = addToChain

    fun onRemoveFromChain(): Action1<Int> = removeFromChain

    fun onMoveUp(): Action1<Int> = moveUp

    fun onMoveDown(): Action1<Int> = moveDown

    fun initialize() {
        state.call(State(emptyList(), emptyList()))
        refresh.call(Unit)
    }
}
