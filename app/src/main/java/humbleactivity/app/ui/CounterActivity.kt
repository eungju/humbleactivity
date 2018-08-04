package humbleactivity.app.ui

import android.app.Activity
import android.os.Bundle
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import humbleactivity.app.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_counter.*
import javax.inject.Inject

class CounterActivity : Activity() {
    @Inject
    lateinit var presenter: Counter
    private lateinit var subscriptions: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_counter)

        presenter = Counter(0)
        subscriptions = CompositeDisposable()
        subscriptions.add(presenter.count.map { it.toString() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RxTextView.text(current)))
        subscriptions.add(RxView.clicks(up).map { Unit }.subscribe(presenter.up))
        subscriptions.add(RxView.clicks(down).map { Unit }.subscribe(presenter.down))
    }

    override fun onDestroy() {
        subscriptions.dispose()
        super.onDestroy()
    }
}
