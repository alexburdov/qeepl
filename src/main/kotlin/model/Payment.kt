package ru.alex.burdovitsin.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.*

@Document(collection = "payments")
data class Payment(
    @Id
    val id: String = UUID.randomUUID().toString(),
    val bookingId: String,
    val bookingToken: String,
    val userId: String,
    val amount: Double,
    val currency: String,
    var status: PaymentStatus = PaymentStatus.PENDING,
    val cardNumber: String,
    val cardHolder: String,
    val cardExpiry: String,
    val cvv: String,
    val provider: PaymentProvider,
    val providerReference: String? = null,
    val providerResponse: Map<String, Any>? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentStatus {
    PENDING, SUCCESS, FAILED
}

enum class PaymentProvider {
    RUSSIA, INTERNATIONAL
}