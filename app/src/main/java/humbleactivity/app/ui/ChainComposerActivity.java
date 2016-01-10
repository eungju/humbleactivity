package humbleactivity.app.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import humbleactivity.app.HumbleApplication;
import humbleactivity.app.HumbleComponent;
import humbleactivity.app.R;
import humbleactivity.app.data.Filter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ChainComposerActivity extends Activity implements ChainComposer.ChainComposerView {
    @Bind(R.id.available_filters)
    ListView availableFiltersView;
    ArrayAdapter availableFiltersAdapter;

    @Bind(R.id.chain)
    ListView chainView;
    ArrayAdapter chainAdapter;

    @Inject
    ChainComposer presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        HumbleComponent component = HumbleApplication.get(this).component();
        component.inject(this);

        setContentView(R.layout.activity_chain_composer);
        ButterKnife.bind(this);

        availableFiltersAdapter = new ArrayAdapter(this, R.layout.view_filter_item);
        availableFiltersView.setAdapter(availableFiltersAdapter);

        chainAdapter = new ArrayAdapter(this, R.layout.view_filter_item);
        chainView.setAdapter(chainAdapter);

        presenter.attach(this);
        presenter.initialize();
    }

    @Override
    protected void onDestroy() {
        presenter.detach();
        super.onDestroy();
    }

    @OnClick(R.id.refresh)
    public void onRefresh(View view) {
        presenter.refresh();
    }

    @OnClick(R.id.add_to_chain)
    public void onAddToChain(View view) {
        int position = availableFiltersView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            presenter.addToChain(position);
        }
    }

    @OnClick(R.id.remove_from_chain)
    public void onRemoveFromChain(View view) {
        int position = chainView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            presenter.removeFromChain(position);
        }
    }

    @OnClick(R.id.move_up)
    public void onMoveUp(View view) {
        int position = chainView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            presenter.moveUpFilter(position);
        }
    }

    @OnClick(R.id.move_down)
    public void onMoveDown(View view) {
        int position = chainView.getCheckedItemPosition();
        if (position != ListView.INVALID_POSITION) {
            presenter.moveDownFilter(position);
        }
    }

    @Override
    public void setAvailableFilters(List<Filter> filters) {
        List<String> items = new ArrayList<>(filters.size());
        for (Filter each : filters) {
            items.add(each.name);
        }
        availableFiltersAdapter.clear();
        availableFiltersAdapter.addAll(items);
    }

    @Override
    public void setChain(List<Filter> chain) {
        List<String> items = new ArrayList<>(chain.size());
        for (Filter each : chain) {
            items.add(each.name);
        }
        chainAdapter.clear();
        chainAdapter.addAll(items);
    }

    @Override
    public void showErrorMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG);
    }

    @Override
    public void swapFilterInChain(int from, int to) {
        Object picked = chainAdapter.getItem(from);
        chainAdapter.remove(picked);
        chainAdapter.insert(picked, to);
        chainView.setItemChecked(to, true);
    }
}