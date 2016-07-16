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
        dut.availables.subscribe(availablesSubscriber)
        dut.chain.subscribe(chainSubscriber)
        dut.chainCursor.subscribe(chainCursorMoveSubscriber)
        dut.loadError.subscribe(loadErrorSubscriber)
    }

    @Test
    fun initialization() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        val states = mockery.states("load")
        mockery.checking(object : Expectations() {
            init {
                oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)))
                then(states.`is`("completed"))
            }
        })
        dut.initialize()
        synchroniser.waitUntil(states.`is`("completed"))
        availablesSubscriber.assertValues(emptyList(), filters)
        chainSubscriber.assertValues(emptyList(), chain)
    }

    @Test
    fun initializationError() {
        val errorMessage = "error"
        val states = mockery.states("load")
        mockery.checking(object : Expectations() {
            init {
                oneOf(effectorService).listFilters(); will(returnValue(Observable.error<Any>(IOException(errorMessage))))
                then(states.`is`("completed"))
            }
        })
        dut.initialize()
        synchroniser.waitUntil(states.`is`("completed"))
        availablesSubscriber.assertValues(emptyList())
        chainSubscriber.assertValues(emptyList())
        loadErrorSubscriber.assertValue(errorMessage)
    }

    @Test
    fun addToChain() {
        val filters = listOf(Filter("Reverb"), Filter("Distortion"))
        dut._backdoor.call(ChainComposer.State(filters, emptyList()))
        dut.addToChain.call(0)
        availablesSubscriber.assertValues(emptyList(), filters, filters.subList(1, filters.size))
        chainSubscriber.assertValues(emptyList(), emptyList(), filters.subList(0, 1))
    }

    @Test
    fun removeFromChain() {
        val availables = listOf(Filter("Reverb"))
        val chain = listOf(Filter("Distortion"))
        dut._backdoor.call(ChainComposer.State(availables, chain))
        dut.removeFromChain.call(0)
        availablesSubscriber.assertValues(emptyList(), availables, availables + chain)
        chainSubscriber.assertValues(emptyList(), chain, emptyList())
    }

    @Test
    fun refresh() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        val states = mockery.states("load")
        mockery.checking(object : Expectations() {
            init {
                oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)))
                then(states.`is`("completed"))
            }
        })
        dut.refresh.call(Unit)
        synchroniser.waitUntil(states.`is`("completed"), 1000)
        availablesSubscriber.assertValues(emptyList(), filters)
        chainSubscriber.assertValues(emptyList(), chain)
    }

    @Test
    fun moveUpFilter() {
        val chain = listOf(Filter("Reverb"), Filter("Distortion"))
        dut._backdoor.call(ChainComposer.State(emptyList(), chain))
        dut.moveUp.call(1)
        chainSubscriber.assertValues(emptyList(), chain, chain.subList(1, chain.size) + chain[0])
        chainCursorMoveSubscriber.assertValues(0)
    }

    @Test
    fun moveDownFilter() {
        val chain = listOf(Filter("Reverb"), Filter("Distortion"))
        dut._backdoor.call(ChainComposer.State(emptyList(), chain))
        dut.moveDown.call(0)
        chainSubscriber.assertValues(emptyList(), chain, chain.subList(1, chain.size) + chain[0])
        chainCursorMoveSubscriber.assertValues(1)
    }
}
