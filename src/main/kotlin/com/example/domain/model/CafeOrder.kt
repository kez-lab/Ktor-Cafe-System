package com.example.domain.model

import com.example.shared.CafeOrderStatus
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class CafeOrder(
    val orderCode: String,
    val cafeMenuId: Long,
    val cafeUserId: Long,
    val price: Int,
    var status: CafeOrderStatus,
    val orderedAt: LocalDateTime,
    override var id: Long? = null,
) : BaseModel