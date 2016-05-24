package humbleactivity.app.ui

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import com.jakewharton.rxbinding.view.RxView

import butterknife.ButterKnife
import com.jakewharton.rxbinding.widget.RxTextView
import humbleactivity.app.HumbleApplication
import humbleactivity.app.R
import rx.subscriptions.CompositeSubscription

import javax.inject.Inject

class CounterActivity : Activity() {
    @Inject lateinit var presenter: Counter
    private lateinit var subscriptions: CompositeSubscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        subscriptions = CompositeSubscription()

        setContentView(R.layout.activity_counter)
        val currentView = ButterKnife.findById<TextView>(this, R.id.current)
        val upView = ButterKnife.findById<View>(this, R.id.up)
        val downView = ButterKnife.findById<View>(this, R.id.down)

        presenter = Counter()
        subscriptions.add(presenter.current().map { it.toString() }.subscribe(RxTextView.text(currentView)))

        subscriptions.add(RxView.clicks(upView).map { Unit }.subscribe(presenter.onUp()))
        subscriptions.add(RxView.clicks(downView).map { Unit }.subscribe(presenter.onDown()))

        presenter.initialize(0)
    }

    override fun onDestroy() {
        presenter.dispose()
        subscriptions.unsubscribe()
        super.onDestroy()
    }
}