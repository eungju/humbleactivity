package humbleactivity.app;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChainComposerTest {
    @Rule
    public JUnitRuleMockery mockery = new JUnitRuleMockery();
    ChainComposerView view = mockery.mock(ChainComposerView.class);
    ChainComposer dut = new ChainComposer(view);

    @Test
    public void initialize() {
        final List<Filter> filters = Arrays.asList(new Filter("Reverb"));
        final List<Filter> chain = Collections.emptyList();
        mockery.checking(new Expectations() {{
            oneOf(view).setAvailableFilters(filters);
            oneOf(view).setChain(chain);
        }});
        dut.initialize();
    }
}
