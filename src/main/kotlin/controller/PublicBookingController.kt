package ru.alex.burdovitsin.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import ru.alex.burdovitsin.model.Booking
import ru.alex.burdovitsin.model.Payment
import ru.alex.burdovitsin.service.BookingService
import ru.alex.burdovitsin.service.PaymentService

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Public Booking API", description = "API для работы с бронированиями")
class PublicBookingController(
    private val bookingService: BookingService,
    private val paymentService: PaymentService
) {

    @PostMapping
    @Operation(summary = "Создать бронирование")
    @SecurityRequirement(name = "bearerAuth")
    fun createBooking(
        @AuthenticationPrincipal user: UserDetails,
        @RequestBody request: CreateBookingRequest
    ): BookingResponse {
        val booking = bookingService.createBooking(
            userId = user.username,
            amount = request.amount,
            currency = request.currency,
            description = request.description,
            countryCode = request.countryCode
        )

        return BookingResponse(
            token = booking.token,
            bookingId = booking.id,
            status = booking.status.toString(),
            amount = booking.amount,
            currency = booking.currency
        )
    }

    @GetMapping("/{token}")
    @Operation(summary = "Получить статус бронирования")
    @SecurityRequirement(name = "bearerAuth")
    fun getBookingStatus(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable token: String
    ): BookingStatusResponse {
        val booking = bookingService.getBookingByToken(user.username, token)
            ?: throw BookingNotFoundException("Booking not found")

        return BookingStatusResponse(
            bookingId = booking.id,
            status = booking.status.toString(),
            amount = booking.amount,
            currency = booking.currency,
            createdAt = booking.createdAt
        )
    }

    @DeleteMapping("/{token}")
    @Operation(summary = "Отменить бронирование")
    @SecurityRequirement(name = "bearerAuth")
    fun cancelBooking(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable token: String
    ): BookingStatusResponse {
        val booking = bookingService.cancelBooking(user.username, token)
            ?: throw BookingNotFoundException("Booking not found")

        return BookingStatusResponse(
            bookingId = booking.id,
            status = booking.status.toString(),
            amount = booking.amount,
            currency = booking.currency,
            createdAt = booking.createdAt
        )
    }

    @GetMapping
    @Operation(summary = "Получить все бронирования пользователя")
    @SecurityRequirement(name = "bearerAuth")
    fun getUserBookings(@AuthenticationPrincipal user: UserDetails): List<Booking> {
        return bookingService.getUserBookings(user.username)
    }

    @PostMapping("/{token}/pay")
    @Operation(summary = "Оплатить бронирование")
    @SecurityRequirement(name = "bearerAuth")
    fun payBooking(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable token: String,
        @Valid @RequestBody request: PaymentRequest
    ): PaymentResponse {
        val payment = paymentService.processPayment(
            userId = user.username,
            token = token,
            cardNumber = request.cardNumber,
            cardHolder = request.cardHolder,
            cardExpiry = request.cardExpiry,
            cvv = request.cvv
        )

        return PaymentResponse(
            paymentId = payment.id,
            status = payment.status.toString(),
            provider = payment.provider.toString(),
            amount = payment.amount,
            currency = payment.currency
        )
    }

    @GetMapping("/{token}/payment")
    @Operation(summary = "Получить статус платежа")
    @SecurityRequirement(name = "bearerAuth")
    fun getPaymentStatus(
        @AuthenticationPrincipal user: UserDetails,
        @PathVariable token: String
    ): Payment? {
        return paymentService.getPaymentStatus(user.username, token)
    }

    @GetMapping("/payments")
    @Operation(summary = "Получить все платежи пользователя")
    @SecurityRequirement(name = "bearerAuth")
    fun getUserPayments(@AuthenticationPrincipal user: UserDetails): List<Payment> {
        return paymentService.getUserPayments(user.username)
    }

    data class CreateBookingRequest(
        @field:Positive
        val amount: Double,
        @field:NotBlank
        val currency: String,
        @field:NotBlank
        val description: String,
        @field:NotBlank
        val countryCode: String
    )

    data class BookingResponse(
        val token: String,
        val bookingId: String,
        val status: String,
        val amount: Double,
        val currency: String
    )

    data class BookingStatusResponse(
        val bookingId: String,
        val status: String,
        val amount: Double,
        val currency: String,
        val createdAt: java.time.LocalDateTime
    )

    data class PaymentRequest(
        @field:NotBlank
        val cardNumber: String,
        @field:NotBlank
        val cardHolder: String,
        @field:NotBlank
        val cardExpiry: String,
        @field:NotBlank
        val cvv: String
    )

    data class PaymentResponse(
        val paymentId: String,
        val status: String,
        val provider: String,
        val amount: Double,
        val currency: String
    )

    @ResponseStatus(HttpStatus.NOT_FOUND)
    class BookingNotFoundException(message: String) : RuntimeException(message)
}