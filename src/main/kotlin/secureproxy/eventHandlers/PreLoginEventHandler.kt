package secureproxy.eventHandlers

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.PreLoginEvent
import net.kyori.adventure.text.Component
import secureproxy.Manager.Companion.logger
import secureproxy.db.DatabaseManager

class PreLoginEventHandler {

    @Subscribe
    fun onPlayerJoin(event: PreLoginEvent) {
        val username = event.username
        logger.info(username)

        if (!DatabaseManager.getWhitelist(username)) {
            event.result = PreLoginEvent.PreLoginComponentResult.denied(Component.text("You are not whitelisted on this Server. Please ask your Network Administrator to whitelist you."))
            return
        }

        if (!DatabaseManager.getDesiredMode(username)) {
            event.result = PreLoginEvent.PreLoginComponentResult.forceOfflineMode()
            logger.info("Desired Mode is offline Mode. Proceeding with forceOfflineMode: true")
            return
        }
        logger.info("Desired Mode is online Mode. Resuming normal login procedure.")
    }
}