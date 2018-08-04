package humbleactivity.app.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.TextView

import butterknife.ButterKnife
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import humbleactivity.app.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

import javax.inject.Inject

class CounterActivity : Activity() {
    @Inject
    lateinit var presenter: Counter
    private lateinit var subscriptions: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscriptions = CompositeDisposable()

        setContentView(R.layout.activity_counter)
        val currentView = ButterKnife.findById<TextView>(this, R.id.current)
        val upView = ButterKnife.findById<View>(this, R.id.up)
        val downView = ButterKnife.findById<View>(this, R.id.down)

        presenter = Counter(0)
        subscriptions.add(presenter.count.map { it.toString() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RxTextView.text(currentView)))
        subscriptions.add(RxView.clicks(upView).map { Unit }.subscribe(presenter.up))
        subscriptions.add(RxView.clicks(downView).map { Unit }.subscribe(presenter.down))
    }

    override fun onDestroy() {
        subscriptions.dispose()
        super.onDestroy()
    }
}
