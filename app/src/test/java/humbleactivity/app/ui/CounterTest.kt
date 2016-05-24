package humbleactivity.app.ui

import org.junit.Before
import org.junit.Test
import rx.observers.TestSubscriber

class CounterTest {
    val dut = Counter()
    val currentSubscriber = TestSubscriber.create<Int>()

    @Before
    fun setUp() {
        dut.current().subscribe(currentSubscriber)
    }

    @Test
    fun upAndDown() {
        dut.initialize(1)
        dut.onUp().call(Unit)
        dut.onUp().call(Unit)
        dut.onDown().call(Unit)
        currentSubscriber.assertValues(1, 2, 3, 2)
    }
}