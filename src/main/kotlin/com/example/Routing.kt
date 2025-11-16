@file:OptIn(ExperimentalTime::class)

package com.example

import com.example.model.CafeMenu.Companion.dummyDataList
import com.example.shared.CafeOrderStatus
import com.example.shared.OrderDto
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        route("/api") {
            get("/menus") {
                val list = dummyDataList
                call.respond(list)
            }
            post("/orders") {
                val request = call.receive<OrderDto.CreateRequest>()
                val selectedMenu = dummyDataList.first { it.id == request.menuId }
                val order = OrderDto.DisplayResponse(
                    orderCode = "ordercode1",
                    menuName = selectedMenu.name,
                    customerName = "홍길동",
                    price = selectedMenu.price,
                    status = CafeOrderStatus.READY,
                    orderedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    id = 1
                )
                call.respond(order)
            }
            get("/orders/{orderCode}") {
                val orderCode = call.parameters["orderCode"]!!
                val order = OrderDto.DisplayResponse(
                    orderCode = orderCode,
                    menuName = "아이스라떼",
                    customerName = "홍길동",
                    price = 1000,
                    status = CafeOrderStatus.READY,
                    orderedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
                    id = 1
                )
                call.respond(order)
            }
        }
    }
}