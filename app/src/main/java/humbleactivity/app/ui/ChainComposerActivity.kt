package humbleactivity.app.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast

import com.jakewharton.rxbinding.view.RxView

import butterknife.ButterKnife
import humbleactivity.app.HumbleApplication
import humbleactivity.app.R
import rx.subscriptions.CompositeSubscription

import javax.inject.Inject

class ChainComposerActivity : Activity() {
    @Inject lateinit var presenter: ChainComposer
    private lateinit var subscriptions: CompositeSubscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HumbleApplication.get(this).component().inject(this)

        subscriptions = CompositeSubscription()

        setContentView(R.layout.activity_chain_composer)
        val availablesView = ButterKnife.findById<ListView>(this, R.id.availables)
        val availablesAdapter = ArrayAdapter<String>(this, R.layout.view_filter_item)
        availablesView.adapter = availablesAdapter
        val chainView = ButterKnife.findById<ListView>(this, R.id.chain)
        val chainAdapter = ArrayAdapter<String>(this, R.layout.view_filter_item)
        chainView.adapter = chainAdapter
        val refreshView = ButterKnife.findById<View>(this, R.id.refresh)
        val addToChainView = ButterKnife.findById<View>(this, R.id.add_to_chain)
        val removeFromChainView = ButterKnife.findById<View>(this, R.id.remove_from_chain)
        val moveDownView = ButterKnife.findById<View>(this, R.id.move_down)
        val moveUpView = ButterKnife.findById<View>(this, R.id.move_up)

        subscriptions.add(presenter.availables().subscribe { filters ->
            availablesAdapter.clear()
            availablesAdapter.addAll(filters.map { it.name })
        })
        subscriptions.add(presenter.chain().subscribe { filters ->
            chainAdapter.clear()
            chainAdapter.addAll(filters.map { it.name })
        })
        subscriptions.add(presenter.chainCursorMove().subscribe { position -> chainView.setItemChecked(position, true) })
        subscriptions.add(presenter.loadError().subscribe { message -> Toast.makeText(this, message, Toast.LENGTH_LONG).show() })

        subscriptions.add(RxView.clicks(refreshView).map { Unit }.subscribe(presenter.onRefresh()))
        subscriptions.add(RxView.clicks(addToChainView).map { availablesView.checkedItemPosition }.filter { p -> p >= 0 && p < availablesView.count }.subscribe(presenter.onAddToChain()))
        subscriptions.add(RxView.clicks(removeFromChainView).map { chainView.checkedItemPosition }.filter { p -> p >= 0 && p < chainView.count }.subscribe(presenter.onRemoveFromChain()))
        subscriptions.add(RxView.clicks(moveUpView).map { chainView.checkedItemPosition }.filter { p -> p >= 1 && p < chainView.count }.subscribe(presenter.onMoveUp()))
        subscriptions.add(RxView.clicks(moveDownView).map { ignore -> chainView.checkedItemPosition }.filter { p -> p >= 0 && p < chainView.count - 1 }.subscribe(presenter.onMoveDown()))

        presenter.initialize()
    }

    override fun onDestroy() {
        presenter.dispose()
        subscriptions.unsubscribe()
        super.onDestroy()
    }
}
