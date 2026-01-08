package ru.alex.burdovitsin

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class PaymentSystemApplication

fun main(args: Array<String>) {
    runApplication<PaymentSystemApplication>(*args)
}