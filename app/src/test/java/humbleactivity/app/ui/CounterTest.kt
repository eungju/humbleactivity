package humbleactivity.app.ui

import org.junit.Before
import org.junit.Test
import rx.observers.TestSubscriber

class CounterTest {
    val dut = Counter(1)
    val currentSubscriber = TestSubscriber.create<Int>()

    @Before
    fun setUp() {
        dut.count.subscribe(currentSubscriber)
    }

    @Test
    fun upAndDown() {
        dut.up.call(Unit)
        dut.up.call(Unit)
        dut.down.call(Unit)
        currentSubscriber.assertValues(1, 2, 3, 2)
    }
}