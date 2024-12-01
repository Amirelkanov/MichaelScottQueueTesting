package org.example

import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import org.junit.jupiter.api.Test
import java.util.*

class SequentialQueue {
    private val s = LinkedList<Int>()

    fun enqueue(x: Int) {
        s.add(x) // add вообще Boolean возвращает
    }

    // pool(): Returns: the head of this list, or null if this list is empty
    fun dequeue(): Int = s.poll() ?: throw EmptyException("Can't get a value from empty queue.")
}

class MSQueueTest {
    private val q = MSQueue()

    @Operation
    fun enqueue(x: Int) = q.enqueue(x)

    @Operation // Ну говорят, что all exceptions now handled as possible results, так что поверю
    fun dequeue(): Int = q.dequeue()

    @Test
    fun obstructionFreedomTest() =
        ModelCheckingOptions()
            .checkObstructionFreedom(true)
            .check(this::class)

    @Test
    fun singleThreadStressTest() =
        StressOptions()
            .actorsBefore(10)
            .threads(1)
            .actorsPerThread(50)
            .actorsAfter(10)
            .iterations(100)
            .check(this::class)

    @Test
    fun parallelThreadsStressTest() =
        StressOptions()
            .actorsBefore(5)
            .threads(2)
            .actorsPerThread(4)
            .iterations(50)
            .actorsAfter(2)
            .check(this::class)

    @Test
    fun multiThreadStressTest() = // Больше не могу - хип заканчивается...)
        StressOptions()
            .actorsBefore(2)
            .threads(10)
            .actorsPerThread(1)
            .actorsAfter(2)
            .iterations(5)
            .check(this::class)

    @Test
    fun sequentialSpecificationTest() = StressOptions()
        .sequentialSpecification(SequentialQueue::class.java)
        .check(this::class)
}
