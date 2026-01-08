package ru.alex.burdovitsin.task

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.alex.burdovitsin.service.PaymentService

@Component
class PaymentStatusCheckTask(
    private val paymentService: PaymentService
) {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    @Scheduled(fixedDelay = 300000) // 5 минут = 300000 мс
    fun checkPendingPayments() {
        logger.info("Starting scheduled payment status check")

        val pendingPayments = paymentService.getPendingPayments()
        logger.info("Found ${pendingPayments.size} pending payments to check")

        pendingPayments.forEach { payment ->
            scope.launch {
                try {
                    paymentService.checkAndUpdatePaymentStatus(payment)
                } catch (e: Exception) {
                    logger.error("Error checking payment status for ${payment.id}", e)
                }
            }
        }
    }

    @Scheduled(fixedDelay = 3600000) // 1 час
    fun cleanupStaleBookings() {
        logger.info("Checking for stale bookings")
        // Можно добавить логику очистки устаревших бронирований
    }
}