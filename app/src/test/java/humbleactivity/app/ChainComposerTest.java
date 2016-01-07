package humbleactivity.app;

import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import rx.Observable;
import rx.schedulers.Schedulers;

public class ChainComposerTest {
    Synchroniser synchroniser = new Synchroniser();
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery() {{
        setThreadingPolicy(synchroniser);
    }};
    ChainComposerView view = mockery.mock(ChainComposerView.class);
    EffectorService effectorService = mockery.mock(EffectorService.class);
    ChainComposer dut = new ChainComposer(view, effectorService, Schedulers.io(), Schedulers.immediate());

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
}
