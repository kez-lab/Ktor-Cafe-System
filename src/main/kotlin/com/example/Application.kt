package com.example

import com.example.configure.configureDatabase
import com.example.configure.configureRouting
import com.example.configure.configureSerialization
import com.example.domain.CafeMenuTable
import com.example.domain.repository.CafeMenuRepository
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()

    val cafeMenuRepository = CafeMenuRepository(CafeMenuTable)
    configureRouting(cafeMenuRepository)
}
