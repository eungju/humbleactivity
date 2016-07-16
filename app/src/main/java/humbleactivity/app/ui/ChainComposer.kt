package humbleactivity.app.ui

import com.jakewharton.rxrelay.PublishRelay
import humbleactivity.app.RxScheduling
import humbleactivity.app.data.EffectorService
import humbleactivity.app.data.Filter
import humbleactivity.app.data.removeAt
import humbleactivity.app.data.swap
import rx.Observable
import javax.inject.Inject

class ChainComposer
@Inject
constructor(effectorService: EffectorService,
            rxScheduling: RxScheduling) {
    data class State(val availables: List<Filter>, val chain: List<Filter>) {
        fun refresh(availables: List<Filter>): State = State(availables, emptyList())

        fun addToChain(position: Int): State = copy(availables.removeAt(position), chain + availables[position])

        fun removeFromChain(position: Int): State = copy(availables + chain[position], chain.removeAt(position))

        fun moveDown(position: Int): State = copy(chain = chain.swap(position, position + 1))

        fun moveUp(position: Int): State = copy(chain = chain.swap(position, position - 1))
    }

    val _backdoor = PublishRelay.create<State>()
    val refresh = PublishRelay.create<Unit>()
    val addToChain = PublishRelay.create<Int>()
    val removeFromChain = PublishRelay.create<Int>()
    val moveUp = PublishRelay.create<Int>()
    val moveDown = PublishRelay.create<Int>()
    val loadError = PublishRelay.create<String>()
    val chainCursor = PublishRelay.create<Int>()
    val state = Observable.merge(
            _backdoor.map { newState -> { state: State -> newState } },
            refresh.concatMap {
                effectorService.listFilters()
                        .observeOn(rxScheduling.ui)
                        .doOnError { throwable -> loadError.call(throwable.message!!) }
                        .onErrorResumeNext(Observable.empty())
                        .map { response -> { state: State -> state.refresh(response) } }
            },
            addToChain.map { position -> { state: State -> state.addToChain(position) } },
            removeFromChain.map { position -> { state: State -> state.removeFromChain(position) } },
            moveUp.map { position ->
                chainCursor.call(position - 1);
                { state: State -> state.moveUp(position) }
            },
            moveDown.map { position ->
                chainCursor.call(position + 1);
                { state: State -> state.moveDown(position) }
            })
            .scan(State(emptyList(), emptyList()), { state, action -> action(state) })
            .cacheWithInitialCapacity(1)
    val availables: Observable<List<Filter>> = state.map { it.availables }
    val chain: Observable<List<Filter>> = state.map { it.chain }

    fun initialize() {
        refresh.call(Unit)
    }
}
