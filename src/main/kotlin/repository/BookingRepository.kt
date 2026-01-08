package ru.alex.burdovitsin.repository


import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.alex.burdovitsin.model.Booking
import ru.alex.burdovitsin.model.BookingStatus
import java.time.LocalDateTime

@Repository
interface BookingRepository : MongoRepository<Booking, String> {
    fun findByToken(token: String): Booking?
    fun findByUserId(userId: String): List<Booking>
    fun findByStatusAndUpdatedAtBefore(status: BookingStatus, date: LocalDateTime): List<Booking>
    fun findByUserIdAndToken(userId: String, token: String): Booking?
}