package humbleactivity.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;

import java.util.ArrayList;
import java.util.List;

public class ChainComposerActivity extends Activity implements ChainComposerView {
    @Bind(R.id.available_filters)
    ListView availableFiltersView;
    ArrayAdapter availableFiltersAdapter;

    @Bind(R.id.chain)
    ListView chainView;
    ArrayAdapter chainAdapter;

    @Bind(R.id.add_to_chain)
    Button addToChainView;

    ChainComposer presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chain_composer);
        ButterKnife.bind(this);

        availableFiltersAdapter = new ArrayAdapter(this, R.layout.view_filter_item);
        availableFiltersView.setAdapter(availableFiltersAdapter);

        chainAdapter = new ArrayAdapter(this, R.layout.view_filter_item);
        chainView.setAdapter(chainAdapter);

        presenter = new ChainComposer(this);
        presenter.initialize();
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
}
