package com.example

import com.example.configure.configureDatabase
import com.example.configure.configureRouting
import com.example.configure.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    configureDatabase()
    configureSerialization()
    configureRouting()
}
