package humbleactivity.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;

import butterknife.BindView;
import butterknife.ButterKnife;
import humbleactivity.app.HumbleApplication;
import humbleactivity.app.R;
import humbleactivity.app.data.Filter;
import kotlin.Unit;
import kotlin.collections.CollectionsKt;
import rx.subscriptions.CompositeSubscription;

import javax.inject.Inject;

public class ChainComposerActivity extends Activity {
    @Inject ChainComposer presenter;
    private CompositeSubscription subscriptions;

    @BindView(R.id.availables) ListView availablesView;
    ArrayAdapter<String> availablesAdapter;
    @BindView(R.id.chain) ListView chainView;
    ArrayAdapter<String> chainAdapter;
    @BindView(R.id.refresh) View refreshView;
    @BindView(R.id.add_to_chain) View addToChainView;
    @BindView(R.id.remove_from_chain) View removeFromChainView;
    @BindView(R.id.move_down) View moveDownView;
    @BindView(R.id.move_up) View moveUpView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HumbleApplication.get(this).component().inject(this);

        setContentView(R.layout.activity_chain_composer);
        ButterKnife.bind(this);

        subscriptions = new CompositeSubscription();

        availablesAdapter = new ArrayAdapter<>(this, R.layout.view_filter_item);
        availablesView.setAdapter(availablesAdapter);
        chainAdapter = new ArrayAdapter<>(this, R.layout.view_filter_item);
        chainView.setAdapter(chainAdapter);

        presenter.availables().subscribe(filters -> {
            availablesAdapter.clear();
            availablesAdapter.addAll(CollectionsKt.map(filters, Filter::getName));
        });
        presenter.chain().subscribe(filters -> {
            chainAdapter.clear();
            chainAdapter.addAll(CollectionsKt.map(filters, Filter::getName));
        });
        presenter.chainCursorMove().subscribe(position -> {
            chainView.setItemChecked(position, true);
        });
        subscriptions.add(presenter.loadError().subscribe(message -> Toast.makeText(ChainComposerActivity.this, message, Toast.LENGTH_LONG).show()));

        RxView.clicks(refreshView).map(ignore -> Unit.INSTANCE).subscribe(presenter.onRefresh());
        RxView.clicks(addToChainView).map(ignore -> availablesView.getCheckedItemPosition())
                .filter(p -> p >= 0 && p < availablesView.getCount())
                .subscribe(presenter.onAddToChain());
        RxView.clicks(removeFromChainView).map(ignore -> chainView.getCheckedItemPosition())
                .filter(p -> p >= 0 && p < chainView.getCount())
                .subscribe(presenter.onRemoveFromChain());
        RxView.clicks(moveUpView).map(ignore -> chainView.getCheckedItemPosition())
                .filter(p -> p >= 1 && p < chainView.getCount())
                .subscribe(presenter.onMoveUp());
        RxView.clicks(moveDownView).map(ignore -> chainView.getCheckedItemPosition())
                .filter(p -> p >= 0 && p < chainView.getCount() - 1)
                .subscribe(presenter.onMoveDown());

        presenter.initialize();
    }

    @Override
    protected void onDestroy() {
        presenter.dispose();
        subscriptions.unsubscribe();
        super.onDestroy();
    }
}
