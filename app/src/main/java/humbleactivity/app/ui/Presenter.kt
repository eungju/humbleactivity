package humbleactivity.app.ui

import rx.subscriptions.CompositeSubscription
import timber.log.Timber

abstract class Presenter<ViewType : PassiveView> {
    protected var view: ViewType? = null
    protected lateinit var subscriptions: CompositeSubscription

    open fun attach(view: ViewType) {
        Timber.d("Attaching %s", view.toString())
        this.view = view
        this.subscriptions = CompositeSubscription()
    }

    fun detach() {
        Timber.d("Detaching %s", view!!.toString())
        subscriptions.unsubscribe()
        view = null
    }
}
