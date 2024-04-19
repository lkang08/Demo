package com.example.demo

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

class ThreadTest {
    var lock = ReentrantLock()
    var condition1: Condition = lock.newCondition()
    var condition2 = lock.newCondition()
    var condition3 = lock.newCondition()
    var state = "A"

    private fun printA() {
        lock.lock()
        try {
            while (state != "A") {
                condition1.await()
            }
            println("${Thread.currentThread().name} $state")
            state = "B"
            condition2.signal()
        } finally {
            lock.unlock()
        }
    }
    private fun printB() {
        lock.lock()
        try {
            while (state != "B") {
                condition2.await()
            }
            println("${Thread.currentThread().name} $state")
            state = "C"
            condition3.signal()
        } finally {
            lock.unlock()
        }
    }
    private fun printC() {
        lock.lock()
        try {
            while (state != "C") {
                condition3.await()
            }
            println("${Thread.currentThread().name} $state")
            state = "A"
            condition1.signal()
        } finally {
            lock.unlock()
        }
    }

    fun test() {
        var t1 = Thread {
            for (i in 0..10) {
                printA()
            }
        }
        var t2 = Thread {
            for (i in 0..10) printB()
        }
        var t3 = Thread {
            for (i in 0..10) printC()
        }
        println("begin")
        t1.start()
        t2.start()
        t3.start()
        t1.join()
        t3.join()
        println("end#############")
    }
}