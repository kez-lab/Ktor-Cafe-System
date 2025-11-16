package com.example.configure

import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory
import java.net.URI

private val logger = LoggerFactory.getLogger("DatabaseConfig")

data class DatabaseConfig(
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
    val database: String,
    val queryParams: String
)

fun parseDatabaseConfig(): DatabaseConfig {
    val dotenv = dotenv { ignoreIfMissing = true }
    val databaseUrlRaw = dotenv["DATABASE_URL"]
        ?: throw IllegalStateException("DATABASE_URL not found in .env file")

    val normalized = databaseUrlRaw.replaceFirst("postgres://", "postgresql://")

    val uri = runCatching { URI(normalized) }.getOrElse {
        throw IllegalStateException("Invalid DATABASE_URL format: ${it.message}", it)
    }

    val userInfo = uri.userInfo
        ?: throw IllegalStateException("DATABASE_URL must include user:password")

    val username = userInfo.substringBefore(":")
    val password = userInfo.substringAfter(":", "")

    require(username.isNotBlank()) { "Username cannot be blank" }
    require(password.isNotBlank()) { "Password cannot be blank" }

    val host = uri.host ?: throw IllegalStateException("Invalid host in DATABASE_URL")
    val port = if (uri.port == -1) 5432 else uri.port
    val database = uri.path?.trimStart('/')?.ifEmpty { "postgres" } ?: "postgres"

    val queryParams = buildQueryParameters(uri.query)

    logger.debug("Database configuration parsed: host=$host, port=$port, database=$database")

    return DatabaseConfig(
        username = username,
        password = password,
        host = host,
        port = port,
        database = database,
        queryParams = queryParams
    )
}

fun buildQueryParameters(existingQuery: String?): String {
    return buildString {
        append("sslmode=require")
        append("&preferQueryMode=simple")

        if (!existingQuery.isNullOrBlank()) {
            append("&").append(existingQuery)
        }
    }
}
