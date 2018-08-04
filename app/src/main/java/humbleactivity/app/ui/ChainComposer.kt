package humbleactivity.app.ui

import com.jakewharton.rxrelay2.PublishRelay
import humbleactivity.app.data.EffectorService
import humbleactivity.app.data.Filter
import humbleactivity.app.data.removeAt
import humbleactivity.app.data.swap
import io.reactivex.Observable
import javax.inject.Inject

class ChainComposer
@Inject
constructor(effectorService: EffectorService) {
    data class State(val availables: List<Filter>, val chain: List<Filter>) {
        fun refresh(availables: List<Filter>): State = State(availables, emptyList())

        fun addToChain(position: Int): State = copy(availables.removeAt(position), chain + availables[position])

        fun removeFromChain(position: Int): State = copy(availables + chain[position], chain.removeAt(position))

        fun moveDown(position: Int): State = copy(chain = chain.swap(position, position + 1))

        fun moveUp(position: Int): State = copy(chain = chain.swap(position, position - 1))
    }

    val _backdoor: PublishRelay<State> = PublishRelay.create()
    val refresh: PublishRelay<Unit> = PublishRelay.create()
    val addToChain: PublishRelay<Int> = PublishRelay.create()
    val removeFromChain: PublishRelay<Int> = PublishRelay.create()
    val moveUp: PublishRelay<Int> = PublishRelay.create()
    val moveDown: PublishRelay<Int> = PublishRelay.create()
    val loadError: PublishRelay<String> = PublishRelay.create()
    val chainCursor: PublishRelay<Int> = PublishRelay.create()
    val state: Observable<State> = Observable.mergeArray(
            _backdoor.map { newState -> { _: State -> newState } },
            refresh.concatMap {
                effectorService.listFilters()
                        .doOnError { throwable -> loadError.accept(throwable.message!!) }
                        .onErrorResumeNext(Observable.empty())
                        .map { response -> { state: State -> state.refresh(response) } }
            },
            addToChain.map { position -> { state: State -> state.addToChain(position) } },
            removeFromChain.map { position -> { state: State -> state.removeFromChain(position) } },
            moveUp.map { position ->
                { state: State ->
                    chainCursor.accept(position - 1);
                    state.moveUp(position)
                }
            },
            moveDown.map { position ->
                { state: State ->
                    chainCursor.accept(position + 1);
                    state.moveDown(position)
                }
            })
            .scan(State(emptyList(), emptyList()), { state: State, action: (State) -> State -> action(state) })
            .cacheWithInitialCapacity(1)
    val availables: Observable<List<Filter>> = state.map { it.availables }
    val chain: Observable<List<Filter>> = state.map { it.chain }

    fun initialize() {
        refresh.accept(Unit)
    }
}
