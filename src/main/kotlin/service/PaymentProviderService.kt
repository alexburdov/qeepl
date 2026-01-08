package ru.alex.burdovitsin.service

import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.alex.burdovitsin.model.Payment
import ru.alex.burdovitsin.model.PaymentProvider
import ru.alex.burdovitsin.model.PaymentStatus
import java.util.*

@Service
class PaymentProviderService {

    private val logger = LoggerFactory.getLogger(javaClass)

    suspend fun processPayment(payment: Payment): Payment {
        logger.info("Processing payment ${payment.id} via ${payment.provider}")

        return when (payment.provider) {
            PaymentProvider.RUSSIA -> processRussianPayment(payment)
            PaymentProvider.INTERNATIONAL -> processInternationalPayment(payment)
        }
    }

    suspend fun checkPaymentStatus(payment: Payment): PaymentStatus {
        logger.info("Checking payment status for ${payment.id}")

        // Имитация проверки статуса у провайдера
        return when (payment.provider) {
            PaymentProvider.RUSSIA -> {
                // Российский провайдер всегда быстро отвечает
                when (Random().nextInt(100)) {
                    in 0..70 -> PaymentStatus.SUCCESS
                    in 71..90 -> PaymentStatus.FAILED
                    else -> PaymentStatus.PENDING
                }
            }
            PaymentProvider.INTERNATIONAL -> {
                // Международный провайдер может эмулировать задержки
                delay(Random().nextLong(100, 1000)) // Задержка 100-1000мс
                when (Random().nextInt(100)) {
                    in 0..60 -> PaymentStatus.SUCCESS
                    in 61..85 -> PaymentStatus.FAILED
                    else -> PaymentStatus.PENDING
                }
            }
        }
    }

    private suspend fun processRussianPayment(payment: Payment): Payment {
        // Быстрая обработка для российского провайдера
        delay(Random().nextLong(50, 300))

        return when (Random().nextInt(100)) {
            in 0..75 -> {
                payment.copy(
                    status = PaymentStatus.SUCCESS,
                    providerReference = "RU_${System.currentTimeMillis()}",
                    providerResponse = mapOf("code" to "00", "message" to "Approved")
                )
            }
            in 76..90 -> {
                payment.copy(
                    status = PaymentStatus.FAILED,
                    providerReference = "RU_${System.currentTimeMillis()}",
                    providerResponse = mapOf("code" to "51", "message" to "Insufficient funds")
                )
            }
            else -> {
                payment.copy(
                    status = PaymentStatus.PENDING,
                    providerReference = "RU_${System.currentTimeMillis()}",
                    providerResponse = mapOf("code" to "99", "message" to "Processing")
                )
            }
        }
    }

    private suspend fun processInternationalPayment(payment: Payment): Payment {
        // Медленная обработка для международного провайдера с возможностью задержек
        delay(Random().nextLong(500, 2000))

        return when (Random().nextInt(100)) {
            in 0..65 -> {
                payment.copy(
                    status = PaymentStatus.SUCCESS,
                    providerReference = "INT_${System.currentTimeMillis()}",
                    providerResponse = mapOf("code" to "000", "message" to "Transaction successful")
                )
            }
            in 66..85 -> {
                payment.copy(
                    status = PaymentStatus.FAILED,
                    providerReference = "INT_${System.currentTimeMillis()}",
                    providerResponse = mapOf("code" to "500", "message" to "Declined")
                )
            }
            else -> {
                payment.copy(
                    status = PaymentStatus.PENDING,
                    providerReference = "INT_${System.currentTimeMillis()}",
                    providerResponse = mapOf("code" to "001", "message" to "Under review")
                )
            }
        }
    }
}