package ru.alex.burdovitsin.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.alex.burdovitsin.model.Payment
import ru.alex.burdovitsin.model.PaymentStatus
import java.time.LocalDateTime

@Repository
interface PaymentRepository : MongoRepository<Payment, String> {
    fun findByBookingToken(token: String): Payment?
    fun findByStatus(status: PaymentStatus): List<Payment>
    fun findByStatusAndUpdatedAtBefore(status: PaymentStatus, date: LocalDateTime): List<Payment>
    fun findByUserId(userId: String): List<Payment>
    fun findByBookingId(bookingId: String): Payment?
}