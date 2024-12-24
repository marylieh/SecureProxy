package secureproxy.eventHandlers

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.util.GameProfile
import com.velocitypowered.api.util.UuidUtils
import net.kyori.adventure.text.Component
import secureproxy.Manager.Companion.logger
import secureproxy.db.DatabaseManager

class LoginEventHandler {

    @Subscribe
    fun onLogin(event: LoginEvent) {
        val player = event.player
        val username = player.username
        val originalProfile = player.gameProfile
        logger.info("${event.result}")

        if (isOnlineMode(originalProfile)) {
            if (!DatabaseManager.getUser(username)) {
                DatabaseManager.registerUser(username, isOnlineMode(originalProfile))
            }
        } else {
            if (!DatabaseManager.getUser(username)) {
                DatabaseManager.registerUser(username, isOnlineMode(originalProfile))
                logger.warning("Registered Offline user. Please note that this User can be impersonated!")
            }
        }

        if (DatabaseManager.getBan(player.uniqueId.toString())) {
            if (DatabaseManager.getBanTime(player.uniqueId.toString()) < System.currentTimeMillis()) {
                DatabaseManager.unbanPlayer(player.uniqueId)
                return
            }
            player.disconnect(Component.text("You have been banned for 4 hours. Please try again later."))
            return
        }

        if (DatabaseManager.getOnlineMode(username) != isOnlineMode(originalProfile)) {
            event.player.disconnect(Component.text("You are not allowed to impersonate online players!"))
            return
        }
    }

    private fun isOnlineMode(gameProfile: GameProfile): Boolean {
        val offlineUUID = UuidUtils.generateOfflinePlayerUuid(gameProfile.name)
        logger.info("Offline UUID: $offlineUUID")
        logger.info("Online UUID: ${gameProfile.id}")
        return gameProfile.id != offlineUUID
    }
}