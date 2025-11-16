package com.example.domain.repository

import com.example.domain.CafeOrderTable
import com.example.domain.model.CafeOrder
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.statements.InsertStatement
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class CafeOrderRepository(
    override val table: CafeOrderTable,
) : ExposedCrudRepository<CafeOrderTable, CafeOrder> {
    override fun toRow(domain: CafeOrder): CafeOrderTable.(InsertStatement<EntityID<Long>>) -> Unit = {
        if (domain.id != null) {
            it[id] = requireNotNull(domain.id) { "Domain id must not be null" }
        }
        it[orderCode] = domain.orderCode
        it[cafeUserId] = domain.cafeUserId
        it[cafeMenuId] = domain.cafeMenuId
        it[price] = domain.price
        it[status] = domain.status
        it[orderedAt] = domain.orderedAt
    }

    override fun toDomain(row: ResultRow): CafeOrder {
        return CafeOrder(
            orderCode = row[CafeOrderTable.orderCode],
            cafeMenuId = row[CafeOrderTable.cafeMenuId].value,
            cafeUserId = row[CafeOrderTable.cafeUserId].value,
            price = row[CafeOrderTable.price],
            status = row[CafeOrderTable.status],
            orderedAt = row[CafeOrderTable.orderedAt],
        ).apply {
            id = row[CafeOrderTable.id].value
        }
    }

    override fun updateRow(domain: CafeOrder): CafeOrderTable.(UpdateStatement) -> Unit = {
        it[orderCode] = domain.orderCode
        it[cafeUserId] = domain.cafeUserId
        it[cafeMenuId] = domain.cafeMenuId
        it[price] = domain.price
        it[status] = domain.status
        it[orderedAt] = domain.orderedAt
    }
}