package humbleactivity.app.ui

import humbleactivity.app.RxScheduling
import humbleactivity.app.data.EffectorService
import humbleactivity.app.data.Filter
import org.jmock.Expectations
import org.jmock.auto.Mock
import org.jmock.integration.junit4.JUnitRuleMockery
import org.jmock.lib.concurrent.Synchroniser
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import rx.Observable
import rx.observers.TestSubscriber
import rx.schedulers.Schedulers
import java.io.IOException

class ChainComposerTest {
    internal var synchroniser = Synchroniser()
    @Rule @JvmField var mockery: JUnitRuleMockery = object : JUnitRuleMockery() {
        init {
            setThreadingPolicy(synchroniser)
        }
    }
    @Mock lateinit var effectorService: EffectorService
    lateinit var dut: ChainComposer
    val availablesSubscriber = TestSubscriber.create<List<Filter>>()
    val chainSubscriber = TestSubscriber.create<List<Filter>>()
    val chainCursorMoveSubscriber = TestSubscriber.create<Int>()
    val loadErrorSubscriber = TestSubscriber.create<String>()

    @Before
    fun setUp() {
        dut = ChainComposer(effectorService, RxScheduling(Schedulers.trampoline(), Schedulers.immediate()))
        dut.availables().subscribe(availablesSubscriber)
        dut.chain().subscribe(chainSubscriber)
        dut.chainCursorMove().subscribe(chainCursorMoveSubscriber)
        dut.loadError().subscribe(loadErrorSubscriber)
    }

    @Test
    fun initialization() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        val states = mockery.states("listFilters")
        mockery.checking(object : Expectations() {
            init {
                oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)))
                then(states.`is`("called"))
            }
        })
        dut.initialize()
        synchroniser.waitUntil(states.`is`("called"))
        availablesSubscriber.assertValues(emptyList(), filters)
        chainSubscriber.assertValues(emptyList(), chain)
    }

    @Test
    fun initializationError() {
        val errorMessage = "error"
        val states = mockery.states("listFilters")
        mockery.checking(object : Expectations() {
            init {
                oneOf(effectorService).listFilters(); will(returnValue(Observable.error<Any>(IOException(errorMessage))))
                then(states.`is`("called"))
            }
        })
        dut.initialize()
        synchroniser.waitUntil(states.`is`("called"))
        availablesSubscriber.assertValues(emptyList())
        chainSubscriber.assertValues(emptyList())
        loadErrorSubscriber.assertValue(errorMessage)
    }

    @Test
    fun addToChain() {
        val filters = listOf(Filter("Reverb"), Filter("Distortion"))
        dut.state.call(ChainComposer.State(filters, emptyList()))
        dut.onAddToChain().call(0)
        availablesSubscriber.assertValues(filters, filters.subList(1, filters.size))
        chainSubscriber.assertValues(emptyList(), filters.subList(0, 1))
    }

    @Test
    fun removeFromChain() {
        val availables = listOf(Filter("Reverb"))
        val chain = listOf(Filter("Distortion"))
        dut.state.call(ChainComposer.State(availables, chain))
        dut.onRemoveFromChain().call(0)
        availablesSubscriber.assertValues(availables, availables + chain)
        chainSubscriber.assertValues(chain, emptyList())
    }

    @Test
    fun refresh() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        val states = mockery.states("listFilters")
        mockery.checking(object : Expectations() {
            init {
                oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)))
                then(states.`is`("called"))
            }
        })
        dut.onRefresh().call(Unit)
        synchroniser.waitUntil(states.`is`("called"), 1000)
        availablesSubscriber.assertValues(filters)
        chainSubscriber.assertValues(chain)
    }

    @Test
    fun moveUpFilter() {
        val chain = listOf(Filter("Reverb"), Filter("Distortion"))
        dut.state.call(ChainComposer.State(emptyList(), chain))
        dut.onMoveUp().call(1)
        chainSubscriber.assertValues(chain, chain.subList(1, chain.size) + chain[0])
        chainCursorMoveSubscriber.assertValues(0)
    }

    @Test
    fun moveDownFilter() {
        val chain = listOf(Filter("Reverb"), Filter("Distortion"))
        dut.state.call(ChainComposer.State(emptyList(), chain))
        dut.onMoveDown().call(0)
        chainSubscriber.assertValues(chain, chain.subList(1, chain.size) + chain[0])
        chainCursorMoveSubscriber.assertValues(1)
    }
}
