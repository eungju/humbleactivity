package humbleactivity.app.ui

import io.reactivex.observers.TestObserver
import org.junit.Before
import org.junit.Test

class CounterTest {
    val dut = Counter(1)
    lateinit var currentSubscriber: TestObserver<Int>

    @Before
    fun setUp() {
        currentSubscriber = dut.count.test()
    }

    @Test
    fun upAndDown() {
        dut.up.accept(Unit)
        dut.up.accept(Unit)
        dut.down.accept(Unit)
        currentSubscriber.assertValues(1, 2, 3, 2)
    }
}