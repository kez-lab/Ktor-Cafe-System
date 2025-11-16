package com.example.configure

import com.zaxxer.hikari.HikariConfig
import io.github.cdimascio.dotenv.dotenv
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("HikariPoolConfig")

fun createHikariConfig(config: DatabaseConfig): HikariConfig {
    val dotenv = dotenv { ignoreIfMissing = true }

    return HikariConfig().apply {
        jdbcUrl = "jdbc:postgresql://${config.host}:${config.port}/${config.database}?${config.queryParams}"
        username = config.username
        password = config.password
        driverClassName = "org.postgresql.Driver"

        maximumPoolSize = dotenv["DB_POOL_SIZE"]?.toIntOrNull() ?: 5
        minimumIdle = dotenv["DB_MIN_IDLE"]?.toIntOrNull() ?: 1

        connectionTimeout = dotenv["DB_CONNECTION_TIMEOUT"]?.toLongOrNull() ?: 10_000
        idleTimeout = dotenv["DB_IDLE_TIMEOUT"]?.toLongOrNull() ?: 60_000
        maxLifetime = dotenv["DB_MAX_LIFETIME"]?.toLongOrNull() ?: (10 * 60_000)

        connectionTestQuery = "SELECT 1"
        validationTimeout = 5_000

        poolName = "CafeSystemPool"

        addDataSourceProperty("prepareThreshold", "0")
        addDataSourceProperty("ApplicationName", "ktor-cafe-system")

        logger.debug("HikariCP configuration created: maxPoolSize=$maximumPoolSize, minIdle=$minimumIdle")
    }
}
