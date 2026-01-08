package ru.alex.burdovitsin.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.alex.burdovitsin.model.Booking
import ru.alex.burdovitsin.model.BookingStatus
import ru.alex.burdovitsin.model.PaymentProvider
import ru.alex.burdovitsin.repository.BookingRepository
import ru.alex.burdovitsin.repository.PaymentRepository
import java.time.LocalDateTime

@Service
class BookingService(
    private val bookingRepository: BookingRepository,
    private val paymentRepository: PaymentRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun createBooking(userId: String, amount: Double, currency: String,
                      description: String, countryCode: String): Booking {
        val booking = Booking(
            userId = userId,
            amount = amount,
            currency = currency,
            description = description,
            countryCode = countryCode
        )

        return bookingRepository.save(booking).also {
            logger.info("Created booking ${it.id} for user $userId")
        }
    }

    fun getUserBookings(userId: String): List<Booking> {
        return bookingRepository.findByUserId(userId)
    }

    fun getBookingByToken(userId: String, token: String): Booking? {
        return bookingRepository.findByUserIdAndToken(userId, token)
    }

    @Transactional
    fun cancelBooking(userId: String, token: String): Booking? {
        val booking = bookingRepository.findByUserIdAndToken(userId, token)
            ?: return null

        if (booking.status == BookingStatus.PAID) {
            throw IllegalStateException("Cannot cancel already paid booking")
        }

        if (booking.status == BookingStatus.CANCELED) {
            return booking
        }

        booking.status = BookingStatus.CANCELED
        return bookingRepository.save(booking).also {
            logger.info("Canceled booking ${it.id} for user $userId")
        }
    }

    fun determinePaymentProvider(countryCode: String): PaymentProvider {
        return if (countryCode.equals("ru", ignoreCase = true)) {
            PaymentProvider.RUSSIA
        } else {
            PaymentProvider.INTERNATIONAL
        }
    }

    fun getAllBookings(): List<Booking> {
        return bookingRepository.findAll()
    }

    fun findStaleBookings(): List<Booking> {
        val cutoffTime = LocalDateTime.now().minusMinutes(30)
        return bookingRepository.findByStatusAndUpdatedAtBefore(
            BookingStatus.NEW,
            cutoffTime
        )
    }
}