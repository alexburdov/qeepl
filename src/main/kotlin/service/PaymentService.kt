package ru.alex.burdovitsin.service

import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.alex.burdovitsin.model.BookingStatus
import ru.alex.burdovitsin.model.Payment
import ru.alex.burdovitsin.model.PaymentStatus
import ru.alex.burdovitsin.repository.BookingRepository
import ru.alex.burdovitsin.repository.PaymentRepository
import java.time.LocalDateTime

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val bookingRepository: BookingRepository,
    private val bookingService: BookingService,
    private val paymentProviderService: PaymentProviderService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun processPayment(
        userId: String,
        token: String,
        cardNumber: String,
        cardHolder: String,
        cardExpiry: String,
        cvv: String
    ): Payment {
        val booking = bookingRepository.findByToken(token)
            ?: throw IllegalArgumentException("Booking not found")

        if (booking.userId != userId) {
            throw SecurityException("Access denied")
        }

        if (booking.status != BookingStatus.NEW) {
            throw IllegalStateException("Booking cannot be paid")
        }

        val existingPayment = paymentRepository.findByBookingToken(token)
        if (existingPayment != null) {
            return existingPayment
        }

        val provider = bookingService.determinePaymentProvider(booking.countryCode)

        val payment = Payment(
            bookingId = booking.id,
            bookingToken = token,
            userId = userId,
            amount = booking.amount,
            currency = booking.currency,
            cardNumber = maskCardNumber(cardNumber),
            cardHolder = cardHolder,
            cardExpiry = cardExpiry,
            cvv = cvv,
            provider = provider
        )

        val savedPayment = paymentRepository.save(payment)

        // Запускаем асинхронную обработку платежа
        runBlocking {
            val processedPayment = paymentProviderService.processPayment(savedPayment)
            updatePaymentAndBooking(processedPayment)
        }

        return savedPayment
    }

    private fun maskCardNumber(cardNumber: String): String {
        return if (cardNumber.length >= 4) {
            "**** **** **** ${cardNumber.takeLast(4)}"
        } else {
            "****"
        }
    }

    @Transactional
    fun updatePaymentAndBooking(payment: Payment) {
        val updatedPayment = paymentRepository.save(
            payment.copy(updatedAt = LocalDateTime.now())
        )

        if (updatedPayment.status == PaymentStatus.SUCCESS) {
            bookingRepository.findByToken(updatedPayment.bookingToken)?.let { booking ->
                booking.status = BookingStatus.PAID
                bookingRepository.save(booking.copy(updatedAt = LocalDateTime.now()))
            }
        }

        logger.info("Updated payment ${payment.id} status to ${payment.status}")
    }

    fun getPaymentStatus(userId: String, token: String): Payment? {
        val payment = paymentRepository.findByBookingToken(token)
        return if (payment?.userId == userId) payment else null
    }

    fun getUserPayments(userId: String): List<Payment> {
        return paymentRepository.findByUserId(userId)
    }

    fun getAllPayments(): List<Payment> {
        return paymentRepository.findAll()
    }

    fun getPendingPayments(): List<Payment> {
        return paymentRepository.findByStatus(PaymentStatus.PENDING)
    }

    suspend fun checkAndUpdatePaymentStatus(payment: Payment) {
        val newStatus = paymentProviderService.checkPaymentStatus(payment)

        if (payment.status != newStatus) {
            val updatedPayment = payment.copy(
                status = newStatus,
                updatedAt = LocalDateTime.now()
            )
            updatePaymentAndBooking(updatedPayment)
        }
    }
}