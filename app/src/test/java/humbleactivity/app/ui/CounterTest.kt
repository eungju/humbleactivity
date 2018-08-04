package humbleactivity.app.ui

import org.junit.jupiter.api.Test

class CounterTest {
    val dut = Counter(1)
    val currentSubscriber = dut.count.test()

    @Test
    fun upAndDown() {
        dut.up.accept(Unit)
        dut.up.accept(Unit)
        dut.down.accept(Unit)
        currentSubscriber.assertValues(1, 2, 3, 2)
    }
}
