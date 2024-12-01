package org.example

import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.junit.jupiter.api.Test


class MSQueueTest {
    private val q = MSQueue()

    @Operation
    fun enqueue(x: Int) = q.enqueue(x)

    @Operation // Ну говорят, что all exceptions now handled as possible results, так что поверю
    fun dequeue(): Int = q.dequeue()

    @Test
    fun obstructionFreedomTest() {
        ModelCheckingOptions()
            .checkObstructionFreedom(true)
            .check(this::class)
    }
}