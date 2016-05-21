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
constructor(private val effectorService: EffectorService,
            private val rxScheduling: RxScheduling) : Presenter<ChainComposer.View>() {
    interface View : PassiveView {
        fun setAvailableFilters(filters: List<Filter>)

        fun setChain(chain: List<Filter>)

        fun showError(message: String)

        fun swapFilterInChain(from: Int, to: Int)
    }

    private val refreshRelay = PublishRelay.create<Unit>()
    lateinit var availableFilters: List<Filter>
    lateinit var chain: List<Filter>

    override fun attach(view: View) {
        super.attach(view)
        subscriptions.add(refreshRelay
                .flatMap {
                    rxScheduling.subscribeOnIoObserveOnUi(effectorService.listFilters())
                            .doOnError { throwable -> view!!.showError(throwable.message!!) }
                            .onErrorResumeNext(Observable.empty())
                }
                .subscribe { filters ->
                    availableFilters = filters
                    chain = emptyList()
                    view.setAvailableFilters(availableFilters)
                    view.setChain(chain)
                })
    }

    fun initialize() {
        availableFilters = emptyList()
        chain = emptyList()
        view!!.setAvailableFilters(availableFilters)
        view!!.setChain(chain)
        refresh()
    }

    fun refresh() {
        refreshRelay.call(Unit)
    }

    fun addToChain(selectedItemPosition: Int) {
        if (selectedItemPosition < 0 || selectedItemPosition >= availableFilters.size) {
            return
        }
        val picked = availableFilters[selectedItemPosition]
        availableFilters = availableFilters.removeAt(selectedItemPosition)
        chain = chain + picked
        view!!.setAvailableFilters(availableFilters)
        view!!.setChain(chain)
    }

    fun removeFromChain(selectedItemPosition: Int) {
        if (selectedItemPosition < 0 || selectedItemPosition >= chain.size) {
            return
        }
        val picked = chain[selectedItemPosition]
        chain = chain.removeAt(selectedItemPosition)
        availableFilters = availableFilters + picked
        view!!.setAvailableFilters(availableFilters)
        view!!.setChain(chain)
    }

    fun moveUpFilter(index: Int) {
        if (index < 1 || index >= chain.size) {
            return
        }
        chain = chain.swap(index, index - 1)
        view!!.swapFilterInChain(index, index - 1)
    }

    fun moveDownFilter(index: Int) {
        if (index < 0 || index >= chain.size - 1) {
            return
        }
        chain = chain.swap(index, index + 1)
        view!!.swapFilterInChain(index, index + 1)
    }
}
