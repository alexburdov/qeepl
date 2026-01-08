package ru.alex.burdovitsin.controller
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import ru.alex.burdovitsin.model.Booking
import ru.alex.burdovitsin.model.Payment
import ru.alex.burdovitsin.service.BookingService
import ru.alex.burdovitsin.service.PaymentService

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin API", description = "Административный интерфейс")
class AdminController(
    private val bookingService: BookingService,
    private val paymentService: PaymentService
) {

    @GetMapping("/bookings")
    @Operation(summary = "Получить все бронирования", security = [SecurityRequirement(name = "bearerAuth")])
    fun getAllBookings(): List<Booking> {
        return bookingService.getAllBookings()
    }

    @GetMapping("/payments")
    @Operation(summary = "Получить все платежи", security = [SecurityRequirement(name = "bearerAuth")])
    fun getAllPayments(): List<Payment> {
        return paymentService.getAllPayments()
    }

    @GetMapping("/bookings/{bookingId}")
    @Operation(summary = "Получить бронирование по ID", security = [SecurityRequirement(name = "bearerAuth")])
    fun getBookingById(@PathVariable bookingId: String): Booking? {
        return bookingService.getAllBookings().find { it.id == bookingId }
    }

    @GetMapping("/payments/{paymentId}")
    @Operation(summary = "Получить платеж по ID", security = [SecurityRequirement(name = "bearerAuth")])
    fun getPaymentById(@PathVariable paymentId: String): Payment? {
        return paymentService.getAllPayments().find { it.id == paymentId }
    }
}