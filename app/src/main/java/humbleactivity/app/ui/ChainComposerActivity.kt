package humbleactivity.app.ui

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.jakewharton.rxbinding2.view.RxView
import humbleactivity.app.HumbleApplication
import humbleactivity.app.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_chain_composer.*
import javax.inject.Inject

class ChainComposerActivity : Activity() {
    @Inject
    lateinit var presenter: ChainComposer
    private lateinit var subscriptions: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HumbleApplication.get(this).component().inject(this)

        setContentView(R.layout.activity_chain_composer)
        val availablesAdapter = ArrayAdapter<String>(this, R.layout.view_filter_item)
        availables.adapter = availablesAdapter
        val chainAdapter = ArrayAdapter<String>(this, R.layout.view_filter_item)
        chain.adapter = chainAdapter

        subscriptions = CompositeDisposable()
        //Output signals
        subscriptions.add(presenter.availables
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { filters ->
                    availablesAdapter.clear()
                    availablesAdapter.addAll(filters.map { it.name })
                })
        subscriptions.add(presenter.chain
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { filters ->
                    chainAdapter.clear()
                    chainAdapter.addAll(filters.map { it.name })
                })
        subscriptions.add(presenter.chainCursor
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { position ->
                    chain.setItemChecked(position, true)
                })
        subscriptions.add(presenter.loadError
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message ->
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                })
        //Input signals
        subscriptions.add(RxView.clicks(refresh).map { Unit }
                .subscribe(presenter.refresh))
        subscriptions.add(RxView.clicks(add_to_chain).map { availables.checkedItemPosition }
                .filter { p -> p >= 0 && p < availables.count }
                .subscribe(presenter.addToChain))
        subscriptions.add(RxView.clicks(remove_from_chain).map { chain.checkedItemPosition }
                .filter { p -> p >= 0 && p < chain.count }
                .subscribe(presenter.removeFromChain))
        subscriptions.add(RxView.clicks(move_up).map { chain.checkedItemPosition }
                .filter { p -> p >= 1 && p < chain.count }
                .subscribe(presenter.moveUp))
        subscriptions.add(RxView.clicks(move_down).map { chain.checkedItemPosition }
                .filter { p -> p >= 0 && p < chain.count - 1 }
                .subscribe(presenter.moveDown))

        presenter.initialize()
    }

    override fun onDestroy() {
        subscriptions.dispose()
        super.onDestroy()
    }
}
