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
import rx.schedulers.Schedulers
import java.io.IOException

class ChainComposerTest {
    internal var synchroniser = Synchroniser()
    @Rule @JvmField var mockery: JUnitRuleMockery = object : JUnitRuleMockery() {
        init {
            setThreadingPolicy(synchroniser)
        }
    }
    @Mock lateinit var view: ChainComposer.View
    @Mock lateinit var effectorService: EffectorService
    lateinit var dut: ChainComposer

    @Before
    fun setUp() {
        dut = ChainComposer(effectorService, RxScheduling(Schedulers.io(), Schedulers.immediate()))
        dut.attach(view)
    }

    @Test
    @Throws(InterruptedException::class)
    fun initialization() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        val states = mockery.states("listFilters")
        mockery.checking(object : Expectations() {
            init {
                oneOf(view).setAvailableFilters(emptyList<Filter>())
                oneOf(view).setChain(emptyList<Filter>())
                oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)))
                oneOf(view).setAvailableFilters(filters)
                oneOf(view).setChain(chain)
                then(states.`is`("called"))
            }
        })
        dut.initialize()
        synchroniser.waitUntil(states.`is`("called"))
    }

    @Test
    @Throws(InterruptedException::class)
    fun initializationFailure() {
        val errorMessage = "error"
        val states = mockery.states("listFilters")
        mockery.checking(object : Expectations() {
            init {
                oneOf(view).setAvailableFilters(emptyList<Filter>())
                oneOf(view).setChain(emptyList<Filter>())
                oneOf(effectorService).listFilters(); will(returnValue(Observable.error<Any>(IOException(errorMessage))))
                oneOf(view).showError(errorMessage)
                then(states.`is`("called"))
            }
        })
        dut.initialize()
        synchroniser.waitUntil(states.`is`("called"))
    }

    @Test
    fun addToChain() {
        val filters = listOf(Filter("Reverb"), Filter("Distortion"))
        mockery.checking(object : Expectations() {
            init {
                oneOf(view).setAvailableFilters(filters.subList(1, filters.size))
                oneOf(view).setChain(filters.subList(0, 1))
            }
        })
        dut.availableFilters = filters
        dut.chain = emptyList()
        dut.addToChain(0)
    }

    @Test
    fun removeFromChain() {
        val filters = listOf(Filter("Reverb"), Filter("Distortion"))
        mockery.checking(object : Expectations() {
            init {
                oneOf(view).setAvailableFilters(filters)
                oneOf(view).setChain(filters.subList(0, 0))
            }
        })
        dut.availableFilters = filters.subList(0, 1)
        dut.chain = filters.subList(1, filters.size)
        dut.removeFromChain(0)
    }

    @Test
    @Throws(InterruptedException::class)
    fun refresh() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        val states = mockery.states("listFilters")
        mockery.checking(object : Expectations() {
            init {
                oneOf(effectorService).listFilters(); will(returnValue(Observable.just(filters)))
                oneOf(view).setAvailableFilters(filters)
                oneOf(view).setChain(chain)
                then(states.`is`("called"))
            }
        })
        dut.refresh()
        synchroniser.waitUntil(states.`is`("called"), 1000)
    }

    @Test
    fun moveUpFilter() {
        val filters = listOf(Filter("Reverb"), Filter("Distortion"))
        mockery.checking(object : Expectations() {
            init {
                oneOf(view).swapFilterInChain(1, 0)
            }
        })
        dut.chain = filters
        dut.moveUpFilter(1)
    }

    @Test
    fun moveDownFilter() {
        val filters = listOf(Filter("Reverb"), Filter("Distortion"))
        mockery.checking(object : Expectations() {
            init {
                oneOf(view).swapFilterInChain(0, 1)
            }
        })
        dut.chain = filters
        dut.moveDownFilter(0)
    }
}
