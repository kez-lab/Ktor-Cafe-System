package com.example.shared

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

class OrderDto {
    @Serializable
    data class CreateRequest(val menuId: Long)

    @Serializable
    data class DisplayResponse(
        val orderCode: String,
        val menuName: String,
        val customerName: String,
        val price: Int,
        var status: CafeOrderStatus,
        val orderedAt: LocalDateTime,
        var id: Long? = null,
    )
}