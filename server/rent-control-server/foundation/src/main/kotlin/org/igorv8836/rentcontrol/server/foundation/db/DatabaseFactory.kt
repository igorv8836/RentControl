package org.igorv8836.rentcontrol.server.foundation.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.ApplicationEnvironment
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object DatabaseFactory {
    private const val DEFAULT_URL = "jdbc:postgresql://localhost:5432/rent_control"
    private const val DEFAULT_USERNAME = "rent_control"
    private const val DEFAULT_PASSWORD = "rent_control"
    private const val DEFAULT_POOL_SIZE = 5

    fun createDataSource(environment: ApplicationEnvironment): HikariDataSource {
        val config = databaseConfig(environment)
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.jdbcUrl
            username = config.username
            password = config.password
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = config.maxPoolSize
        }
        return HikariDataSource(hikariConfig)
    }

    fun connectAndMigrate(dataSource: DataSource): Database {
        val database = Database.connect(dataSource)
        transaction(database) {
            SchemaUtils.createMissingTablesAndColumns(*allTables)
            ensureUsersConstraints()
        }
        return database
    }

    fun checkDatabase(database: Database) {
        transaction(database) {
            exec("SELECT 1") { result -> result.next() }
        }
    }

    private fun Transaction.ensureUsersConstraints() {
        exec("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_status_check")
        exec("ALTER TABLE users ADD CONSTRAINT users_status_check CHECK (status IN ('pending','active','blocked'))")
        exec("ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check")
        exec("ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('admin','landlord','inspector','tenant'))")
    }

    private fun databaseConfig(environment: ApplicationEnvironment): DatabaseConfig {
        fun property(path: String, env: String, default: String): String {
            val fromEnv = System.getenv(env)
            val fromConfig = runCatching { environment.config.propertyOrNull(path)?.getString() }.getOrNull()
            return listOf(fromEnv, fromConfig, default)
                .firstOrNull { it.orEmpty().isNotBlank() }
                ?: default
        }

        fun intProperty(path: String, env: String, default: Int): Int {
            val raw = property(path, env, default.toString())
            return raw.toIntOrNull() ?: default
        }

        return DatabaseConfig(
            jdbcUrl = property("database.url", "RENT_CONTROL_DB_URL", DEFAULT_URL),
            username = property("database.user", "RENT_CONTROL_DB_USER", DEFAULT_USERNAME),
            password = property("database.password", "RENT_CONTROL_DB_PASSWORD", DEFAULT_PASSWORD),
            maxPoolSize = intProperty("database.maxPoolSize", "RENT_CONTROL_DB_POOL_SIZE", DEFAULT_POOL_SIZE),
        )
    }

    private data class DatabaseConfig(
        val jdbcUrl: String,
        val username: String,
        val password: String,
        val maxPoolSize: Int,
    )
}
