package humbleactivity.app;

import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChainComposerTest {
    Synchroniser synchroniser = new Synchroniser();
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery() {{
        setThreadingPolicy(synchroniser);
    }};
    ChainComposer.ChainComposerView view = mockery.mock(ChainComposer.ChainComposerView.class);
    EffectorService effectorService = mockery.mock(EffectorService.class);
    ChainComposer dut = new ChainComposer(effectorService, new RxScheduling(Schedulers.io(), Schedulers.immediate()));

    @Before
    public void setUp() {
        dut.attach(view);
    }

    @Test
    public void initialization() throws InterruptedException {
        final List<Filter> filters = Arrays.asList(new Filter("Reverb"));
        final List<Filter> chain = Collections.emptyList();
        final States states = mockery.states("listFilters");
        mockery.checking(new Expectations() {{
            oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)));
            oneOf(view).setAvailableFilters(filters);
            oneOf(view).setChain(chain);
            then(states.is("called"));
        }});
        dut.initialize();
        synchroniser.waitUntil(states.is("called"));
    }

    @Test
    public void initializationFailure() throws InterruptedException {
        String errorMessage = "error";
        final States states = mockery.states("listFilters");
        mockery.checking(new Expectations() {{
            oneOf(effectorService).listFilters(); will(returnValue(Observable.error(new IOException(errorMessage))));
            oneOf(view).showErrorMessage(errorMessage);
            then(states.is("called"));
        }});
        dut.initialize();
        synchroniser.waitUntil(states.is("called"));
    }

    @Test
    public void addToChain() {
        final List<Filter> filters = Arrays.asList(new Filter("Reverb"), new Filter("Distortion"));
        mockery.checking(new Expectations() {{
            oneOf(view).setAvailableFilters(filters.subList(1, filters.size()));
            oneOf(view).setChain(filters.subList(0, 1));
        }});
        dut.availableFilters = new ArrayList<>(filters);
        dut.chain = new ArrayList<>();
        dut.addToChain(0);
    }

    @Test
    public void removeFromChain() {
        final List<Filter> filters = Arrays.asList(new Filter("Reverb"), new Filter("Distortion"));
        mockery.checking(new Expectations() {{
            oneOf(view).setAvailableFilters(filters);
            oneOf(view).setChain(filters.subList(0, 0));
        }});
        dut.availableFilters = new ArrayList<>();
        dut.availableFilters.addAll(filters.subList(0, 1));
        dut.chain = new ArrayList<>();
        dut.chain.addAll(filters.subList(1, filters.size()));
        dut.removeFromChain(0);
    }

    @Test
    public void refresh() throws InterruptedException {
        final List<Filter> filters = Arrays.asList(new Filter("Reverb"));
        final List<Filter> chain = Collections.emptyList();
        final States states = mockery.states("listFilters");
        mockery.checking(new Expectations() {{
            oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)));
            oneOf(view).setAvailableFilters(filters);
            oneOf(view).setChain(chain);
            then(states.is("called"));
        }});
        dut.refresh();
        synchroniser.waitUntil(states.is("called"), 1000);
    }

    @Test
    public void moveUpFilter() {
        final List<Filter> filters = Arrays.asList(new Filter("Reverb"), new Filter("Distortion"));
        mockery.checking(new Expectations() {{
            oneOf(view).swapFilterInChain(1, 0);
        }});
        dut.chain = new ArrayList<>(filters);
        dut.moveUpFilter(1);
    }

    @Test
    public void moveDownFilter() {
        final List<Filter> filters = Arrays.asList(new Filter("Reverb"), new Filter("Distortion"));
        mockery.checking(new Expectations() {{
            oneOf(view).swapFilterInChain(0, 1);
        }});
        dut.chain = new ArrayList<>(filters);
        dut.moveDownFilter(0);
    }
}
