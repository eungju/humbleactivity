package humbleactivity.app.ui

import humbleactivity.app.data.EffectorService
import humbleactivity.app.data.Filter
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import java.io.IOException

@MockitoSettings
class ChainComposerTest {
    lateinit var effectorService: EffectorService
    lateinit var dut: ChainComposer
    lateinit var availablesSubscriber: TestObserver<List<Filter>>
    lateinit var chainSubscriber: TestObserver<List<Filter>>
    lateinit var chainCursorMoveSubscriber: TestObserver<Int>
    lateinit var loadErrorSubscriber: TestObserver<String>

    @BeforeEach
    fun setUp() {
        effectorService = mock()
        dut = ChainComposer(effectorService)
        availablesSubscriber = dut.availables.test()
        chainSubscriber = dut.chain.test()
        chainCursorMoveSubscriber = dut.chainCursor.test()
        loadErrorSubscriber = dut.loadError.test()
    }

    @Test
    fun initialization() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        stub {
            on(effectorService.listFilters()).thenReturn(Observable.just(filters))
        }
        dut.initialize()
        availablesSubscriber.assertValues(emptyList(), filters)
        chainSubscriber.assertValues(emptyList(), chain)
    }

    @Test
    fun initializationError() {
        val errorMessage = "error"
        stub {
            on(effectorService.listFilters()).thenReturn(Observable.error(IOException(errorMessage)))
        }
        dut.initialize()
        availablesSubscriber.assertValues(emptyList())
        chainSubscriber.assertValues(emptyList())
        loadErrorSubscriber.assertValue(errorMessage)
    }

    @Test
    fun addToChain() {
        val filters = listOf(Filter("Reverb"), Filter("Distortion"))
        dut._backdoor.accept(ChainComposer.State(filters, emptyList()))
        dut.addToChain.accept(0)
        availablesSubscriber.assertValues(emptyList(), filters, filters.subList(1, filters.size))
        chainSubscriber.assertValues(emptyList(), emptyList(), filters.subList(0, 1))
    }

    @Test
    fun removeFromChain() {
        val availables = listOf(Filter("Reverb"))
        val chain = listOf(Filter("Distortion"))
        dut._backdoor.accept(ChainComposer.State(availables, chain))
        dut.removeFromChain.accept(0)
        availablesSubscriber.assertValues(emptyList(), availables, availables + chain)
        chainSubscriber.assertValues(emptyList(), chain, emptyList())
    }

    @Test
    fun refresh() {
        val filters = listOf(Filter("Reverb"))
        val chain = emptyList<Filter>()
        stub {
            on(effectorService.listFilters()).thenReturn(Observable.just(filters))
        }
        dut.refresh.accept(Unit)
        availablesSubscriber.assertValues(emptyList(), filters)
        chainSubscriber.assertValues(emptyList(), chain)
    }

    @Test
    fun moveUpFilter() {
        val chain = listOf(Filter("Reverb"), Filter("Distortion"))
        dut._backdoor.accept(ChainComposer.State(emptyList(), chain))
        dut.moveUp.accept(1)
        chainSubscriber.assertValues(emptyList(), chain, chain.subList(1, chain.size) + chain[0])
        chainCursorMoveSubscriber.assertValues(0)
    }

    @Test
    fun moveDownFilter() {
        val chain = listOf(Filter("Reverb"), Filter("Distortion"))
        dut._backdoor.accept(ChainComposer.State(emptyList(), chain))
        dut.moveDown.accept(0)
        chainSubscriber.assertValues(emptyList(), chain, chain.subList(1, chain.size) + chain[0])
        chainCursorMoveSubscriber.assertValues(1)
    }
}
