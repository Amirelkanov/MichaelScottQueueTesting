package org.example

import java.util.concurrent.atomic.AtomicReference

class EmptyException(message: String) : Exception(message)

class MSQueue {
    private val sentinel = Node(0)
    private val head = AtomicReference(sentinel)
    private val tail = AtomicReference(sentinel)

    fun enqueue(x: Int) {
        val node = Node(x)
        while (true) { // CAS loop
            val last = tail.get()
            val next = last.next.get()
            if (last == tail.get()) {
                if (next == null) {
                    // Меняем у последнего next cas'ом (если не надо править хвост)
                    if (last.next.compareAndSet(null, node)) {
                        // Если успешно, то пробуем править хвост (можем заснуть и быть перехвачены другим потоком)
                        tail.compareAndSet(last, node)
                        return
                    }
                } else {
                    // Если мы перехватили раньше, чем другой поток, то поможем продвинуть отстающий хвост
                    tail.compareAndSet(last, next)
                }
            }
        }
    }

    @Throws(EmptyException::class)
    fun dequeue(): Int {
        while (true) { // CAS loop
            val first = head.get()
            val last = tail.get()
            val next = first.next.get()

            if (first == last) { // Если голова и хвост совпадают
                if (next == null) { // Если очередь содержит только sentinel, то логично, что она пустая, и доставать оттуда нечего
                    throw EmptyException("Can't get a value from empty queue.")
                }
                // Иначе у нас отстающий хвост, который мы попробуем поправить
                tail.compareAndSet(last, next)
            } else {
                // В этом моменте next точно не null, т.к first != last, значит наш хвост находится на какой-то
                // ненулевой ноде => как минимум next у first'а - это last, который, как мы выяснили, != null
                val value = next!!.value
                if (head.compareAndSet(first, next)) { // Если сумели "достать" элемент
                    return value
                }
            }
        }
    }


    class Node(val value: Int, val next: AtomicReference<Node?> = AtomicReference())
}
