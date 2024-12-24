@file:Suppress("SqlSourceToSinkFlow")

package secureproxy.db

import secureproxy.Manager.Companion.logger
import secureproxy.utils.ConfigManager
import secureproxy.utils.unixTimestampToDateTime
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

object DatabaseManager {

    private val connectionString: String = "jdbc:mysql://${ConfigManager.getConfig().getString("database.host") ?: "127.0.0.1"}:${ConfigManager.getConfig().getString("database.port") ?: 3306}/${ConfigManager.getConfig().getString("database.name") ?: "sproxy"}"
    private val user = ConfigManager.getConfig().getString("database.user") ?: "root"
    private val password = ConfigManager.getConfig().getString("database.password") ?: "root"

    fun registerUser(username: String, onlineMode: Boolean) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val insertStatement = "INSERT INTO players (playerName, onlineMode) VALUES ('$username', $onlineMode);"
            val result = statement.executeUpdate(insertStatement)

            logger.info("Registered new user $username in online mode: $onlineMode. SQL Result: $result")

            connection.close()

        } catch (e: SQLException) { e.printStackTrace() }
    }

    fun getUser(username: String): Boolean {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val getUserStatement = "SELECT 1 FROM players WHERE playerName = '$username' LIMIT 1;"
            val result = statement.executeQuery(getUserStatement)
            val resultId = result.next()
            logger.info("Got result: $result with resultID: $resultId")

            connection.close()
            return resultId
        } catch (e: SQLException) { e.printStackTrace() }
        return false
    }

    fun deleteUser(username: String) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val deleteUserStatement = "DELETE FROM players WHERE playerName = '$username';"
            statement.executeUpdate(deleteUserStatement)

            logger.info("Removed $username user registration")
        } catch (e: SQLException) { e.printStackTrace() }
    }

    fun getOnlineMode(username: String): Boolean {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val getOnlineModeStatement = "SELECT onlineMode FROM players WHERE playerName = '$username';"
            val result = statement.executeQuery(getOnlineModeStatement)
            result.next()
            val onlineMode = result.getBoolean("onlineMode")

            connection.close()
            return onlineMode
        } catch (e: SQLException) { e.printStackTrace() }
        return false
    }

    fun getBan(uuid: String): Boolean {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val getBanStatement = "SELECT uuid FROM bans WHERE uuid = '$uuid';"
            val result = statement.executeQuery(getBanStatement)
            val resultId = result.next()

            connection.close()
            return resultId
        } catch (e: SQLException) { e.printStackTrace() }
        return false
    }

    fun getBanTime(uuid: String): Long {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val getBanTimeStatement = "SELECT effectiveNotAfter FROM bans WHERE uuid = '$uuid';"
            val result = statement.executeQuery(getBanTimeStatement)
            result.next()
            val time = result.getLong("effectiveNotAfter")

            connection.close()
            return time
        } catch (e: SQLException) { e.printStackTrace() }
        return 0
    }

    fun getWhitelist(username: String): Boolean {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val getWhitelistStatement = "SELECT playerName FROM whitelist WHERE playerName = '$username';"
            val result = statement.executeQuery(getWhitelistStatement)
            val resultId = result.next()

            connection.close()
            return resultId
        } catch (e: SQLException) { e.printStackTrace() }
        return false
    }

    fun getWhitelistedPlayers(): MutableList<String> {
        val whitelistedPlayers = mutableListOf<String>()
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val query = "SELECT playerName FROM whitelist;"
            val result = statement.executeQuery(query)

            while (result.next()) {
                whitelistedPlayers.add(result.getString("playerName"))
            }

            return whitelistedPlayers
        } catch (e: SQLException) { e.printStackTrace() }
        return whitelistedPlayers
    }

    fun addWhitelist(username: String, desiredMode: Boolean) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val query = "INSERT INTO whitelist (playerName, desiredMode) VALUES ('$username', $desiredMode);"
            statement.executeUpdate(query)

            logger.info("Added $username with desiredMode: $desiredMode to the whitelist.")
        } catch (e: SQLException) { e.printStackTrace() }
    }

    fun removeWhitelist(username: String) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val query = "DELETE FROM whitelist WHERE playerName = '$username';"
            statement.executeUpdate(query)

            logger.info("Removed $username from the whitelist.")
        } catch (e: SQLException) { e.printStackTrace() }
    }

    fun getDesiredMode(username: String): Boolean {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val getDesiredStatement = "SELECT desiredMode FROM whitelist WHERE playerName = '$username';"
            val result = statement.executeQuery(getDesiredStatement)
            result.next()
            val desiredMode = result.getBoolean("desiredMode")

            connection.close()
            return desiredMode
        } catch (e: SQLException) { e.printStackTrace() }
        return false
    }

    fun banPlayer(uuid: UUID, startTime: Long, endTime: Long) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val query = "INSERT INTO bans (uuid, effectiveSince, effectiveNotAfter) VALUES ('$uuid', '$startTime', '$endTime');"
            statement.executeUpdate(query)

            logger.info("Banned $uuid - effective since ${unixTimestampToDateTime(startTime)} - not effective after ${unixTimestampToDateTime(endTime)}")
        } catch (e: SQLException) { e.printStackTrace() }
    }

    fun unbanPlayer(uuid: UUID) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val query = "DELETE FROM bans WHERE uuid = '$uuid';"
            statement.executeUpdate(query)

            logger.info("Unbanned $uuid")
        } catch (e: SQLException) { e.printStackTrace() }
    }

    fun performMigrations() {
        logger.info("Checking for available database migrations..")

        performWhitelistMigrations()
        performPlayersMigration()
        performBansMigration()
    }

    private fun performWhitelistMigrations() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val existsQuery = "SHOW TABLES like 'whitelist';"
            val existsResult = statement.executeQuery(existsQuery)

            if (!existsResult.next()) {
                logger.info("Found pending whitelist migrations.. migrating..")

                val migration = "CREATE TABLE whitelist (ID INT PRIMARY KEY AUTO_INCREMENT, playerName VARCHAR(100) NOT NULL, desiredMode BOOLEAN NOT NULL);"
                val migrationResult = statement.executeUpdate(migration)

                logger.info("Whitelist database migration finished with: $migrationResult")
                connection.close()
                return
            }
        } catch (e: SQLException) { e.printStackTrace() }
    }

    private fun performPlayersMigration() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val existsQuery = "SHOW TABLES like 'players';"
            val existsResult = statement.executeQuery(existsQuery)

            if (!existsResult.next()) {
                logger.info("Found pending players migrations.. migrating..")

                val migration = "CREATE TABLE players (ID INT PRIMARY KEY AUTO_INCREMENT, playerName VARCHAR(100) NOT NULL, onlineMode BOOLEAN NOT NULL);"
                val migrationResult = statement.executeUpdate(migration)

                logger.info("Players database migration finished with: $migrationResult")
                connection.close()
                return
            }
        } catch (e: SQLException) { e.printStackTrace() }
    }

    private fun performBansMigration() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver")
            val connection = DriverManager.getConnection(connectionString, user, password)
            val statement = connection.createStatement()

            val existsQuery = "SHOW TABLES like 'bans';"
            val existsResult = statement.executeQuery(existsQuery)

            if (!existsResult.next()) {
                logger.info("Found pending bans migrations.. migrating..")

                val migration = "CREATE TABLE bans (ID INT PRIMARY KEY AUTO_INCREMENT, uuid VARCHAR(100) NOT NULL, effectiveSince BIGINT NOT NULL, effectiveNotAfter BIGINT NOT NULL);"
                val migrationResult = statement.executeUpdate(migration)

                logger.info("Bans database migration finished with: $migrationResult")
                connection.close()
                return
            }
        } catch (e: SQLException) { e.printStackTrace() }
    }
}
