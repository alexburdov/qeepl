package ru.alex.burdovitsin.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document(collection = "bookings")
data class Booking(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val token: String = UUID.randomUUID().toString(),
    var status: BookingStatus = BookingStatus.NEW,
    val amount: Double,
    val currency: String,
    val description: String,
    val countryCode: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class BookingStatus {
    NEW, PAID, CANCELED
}