package com.example.configure

import com.example.domain.CafeMenuTable
import com.example.domain.CafeOrderTable
import com.example.domain.CafeUserTable
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger("Database")

fun Application.configureDatabase() {
    try {
        val dataSource = createDataSource()
        Database.connect(dataSource)
        initializeSchema()
    } catch (e: Exception) {
        logger.error("Failed to configure database", e)
        throw e
    }
}

private fun createDataSource(): DataSource {
    val config = parseDatabaseConfig()
    return HikariDataSource(createHikariConfig(config))
}

private fun initializeSchema() {
    transaction {
        addLogger(StdOutSqlLogger)

        SchemaUtils.create(
            CafeMenuTable,
            CafeUserTable,
            CafeOrderTable
        )

        logger.info("Schema tables created/verified")
    }
}
