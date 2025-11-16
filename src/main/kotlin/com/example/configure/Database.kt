package com.example.configure

import com.example.domain.CafeMenuTable
import com.example.domain.CafeOrderTable
import com.example.domain.CafeUserTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import io.ktor.server.application.*
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.slf4j.LoggerFactory
import java.net.URI
import javax.sql.DataSource

private val logger = LoggerFactory.getLogger("Database")

/**
 * Database configuration for the application
 * Configures connection pool and initializes schema
 */
fun Application.configureDatabase() {
    logger.info("Initializing database configuration...")

    try {
        val dataSource = createDataSource()
        Database.connect(dataSource)
        logger.info("Database connection established successfully")

        initializeSchema()
        logger.info("Database schema initialized successfully")
    } catch (e: Exception) {
        logger.error("Failed to configure database", e)
        throw e
    }
}

/**
 * Creates and configures HikariCP DataSource from environment variables
 * @return Configured DataSource
 * @throws IllegalStateException if DATABASE_URL is not found or invalid
 */
private fun createDataSource(): DataSource {
    val config = parseDatabaseConfig()
    return HikariDataSource(createHikariConfig(config))
}

/**
 * Database connection configuration
 */
private data class DatabaseConfig(
    val username: String,
    val password: String,
    val host: String,
    val port: Int,
    val database: String,
    val queryParams: String
)

/**
 * Parses DATABASE_URL from environment and extracts connection details
 * @return DatabaseConfig with parsed connection details
 * @throws IllegalStateException if DATABASE_URL is invalid or missing
 */
private fun parseDatabaseConfig(): DatabaseConfig {
    val dotenv = dotenv { ignoreIfMissing = true }
    val databaseUrlRaw = dotenv["DATABASE_URL"]
        ?: throw IllegalStateException("DATABASE_URL not found in .env file")

    logger.debug("Parsing database URL...")

    // Normalize postgres:// to postgresql:// for JDBC compatibility
    val normalized = databaseUrlRaw.replaceFirst("postgres://", "postgresql://")

    // Parse URI
    val uri = runCatching { URI(normalized) }.getOrElse {
        throw IllegalStateException("Invalid DATABASE_URL format: ${it.message}", it)
    }

    // Extract credentials
    val userInfo = uri.userInfo
        ?: throw IllegalStateException("DATABASE_URL must include user:password")

    val username = userInfo.substringBefore(":")
    val password = userInfo.substringAfter(":", "")

    require(username.isNotBlank()) { "Username cannot be blank" }
    require(password.isNotBlank()) { "Password cannot be blank" }

    // Extract connection details
    val host = uri.host ?: throw IllegalStateException("Invalid host in DATABASE_URL")
    val port = if (uri.port == -1) 5432 else uri.port
    val database = uri.path?.trimStart('/')?.ifEmpty { "postgres" } ?: "postgres"

    // Build query parameters with SSL and optimization settings
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

/**
 * Builds query parameters for PostgreSQL connection
 * Adds required SSL and performance settings
 */
private fun buildQueryParameters(existingQuery: String?): String {
    return buildString {
        // Required settings for Supabase Pooler
        append("sslmode=require")
        append("&preferQueryMode=simple")

        // Preserve existing query parameters
        if (!existingQuery.isNullOrBlank()) {
            append("&").append(existingQuery)
        }
    }
}

/**
 * Creates HikariCP configuration with optimized settings
 * @param config Database connection configuration
 * @return Configured HikariConfig
 */
private fun createHikariConfig(config: DatabaseConfig): HikariConfig {
    val dotenv = dotenv { ignoreIfMissing = true }

    return HikariConfig().apply {
        // Connection details
        jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}?${config.queryParams}"
        username = config.username
        password = config.password
        driverClassName = "org.postgresql.Driver"

        // Connection pool settings (configurable via environment)
        maximumPoolSize = dotenv["DB_POOL_SIZE"]?.toIntOrNull() ?: 5
        minimumIdle = dotenv["DB_MIN_IDLE"]?.toIntOrNull() ?: 1

        // Timeout settings (in milliseconds)
        connectionTimeout = dotenv["DB_CONNECTION_TIMEOUT"]?.toLongOrNull() ?: 10_000
        idleTimeout = dotenv["DB_IDLE_TIMEOUT"]?.toLongOrNull() ?: 60_000
        maxLifetime = dotenv["DB_MAX_LIFETIME"]?.toLongOrNull() ?: (10 * 60_000)

        // Connection validation
        connectionTestQuery = "SELECT 1"
        validationTimeout = 5_000

        // Pool name for logging
        poolName = "CafeSystemPool"

        // Supabase Pooler optimization
        // Disable prepared statement caching to avoid issues with connection poolers
        addDataSourceProperty("prepareThreshold", "0")

        // Additional PostgreSQL optimizations
        addDataSourceProperty("ApplicationName", "ktor-cafe-system")

        logger.debug("HikariCP configuration created: maxPoolSize=$maximumPoolSize, minIdle=$minimumIdle")
    }
}

/**
 * Initializes database schema
 * Creates tables if they don't exist
 */
private fun initializeSchema() {
    transaction {
        // Enable SQL logging in development
        addLogger(StdOutSqlLogger)

        // Create tables
        SchemaUtils.create(
            CafeMenuTable,
            CafeUserTable,
            CafeOrderTable
        )

        logger.info("Schema tables created/verified: CafeMenuTable, CafeUserTable, CafeOrderTable")
    }
}
