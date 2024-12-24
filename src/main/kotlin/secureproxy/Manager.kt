package secureproxy

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import com.velocitypowered.api.scheduler.Scheduler
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import secureproxy.commands.DebugCommand
import secureproxy.commands.VoteCommand
import secureproxy.commands.WhitelistCommand
import secureproxy.db.DatabaseManager
import secureproxy.eventHandlers.LoginEventHandler
import secureproxy.eventHandlers.PreLoginEventHandler
import secureproxy.utils.ConfigManager
import java.util.logging.Logger
import javax.inject.Inject

@Plugin(
    id = "secureproxy",
    name = "secure-proxy",
    version = "1.1.0",
    description = "A simple secure proxy which allows non premium players to join but do not allow impersonating other players.",
    url = "https://marylieh.social/",
    authors = ["marylieh"]
)
class Manager {

    companion object {
        lateinit var server: ProxyServer
        lateinit var logger: Logger
        lateinit var prefix: TextComponent
        lateinit var scheduler: Scheduler
        lateinit var instance: Manager
            private set
    }

    @Inject
    @Suppress("Unused")
    fun velocityStartup(server: ProxyServer, logger: Logger) {
        instance = this

        Manager.server = server
        Manager.logger = logger
        scheduler = server.scheduler

        prefix =
            Component.text("●", NamedTextColor.DARK_GRAY)
                .append(Component.space())
                .append(Component.text("SecureProxy", NamedTextColor.BLUE))
                .append(Component.space())
                .append(Component.text("|", NamedTextColor.DARK_GRAY))
                .append(Component.space())
    }

    @Subscribe
    @Suppress("Unused")
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        ConfigManager.initConfig()
        DatabaseManager.performMigrations()

        eventHandlerRegistration()
        commandRegistration()

        logger.info("Secure Proxy has been initialized.")
    }

    private fun eventHandlerRegistration() {
        val eventManager = server.eventManager
        eventManager.register(this, PreLoginEventHandler())
        eventManager.register(this, LoginEventHandler())
    }

    private fun commandRegistration() {
        logger.info("Registering commands")
        val commandManager = server.commandManager
        val whitelistMeta = commandManager.metaBuilder("sw")
            .aliases("securewhitelist", "sproxywhitelist")
            .plugin(this)
            .build()
        val voteMeta = commandManager.metaBuilder("vote")
            .aliases("securevote", "svote", "votekick")
            .plugin(this)
            .build()
        val debugMeta = commandManager.metaBuilder("sd")
            .aliases("securedebug", "sdebug")
            .plugin(this)
            .build()

        val whitelistCommand = WhitelistCommand.createBrigadierCommand(server)
        val voteCommand = VoteCommand.createBrigadierCommand(server)
        val debugCommand = DebugCommand.createBrigadierCommand()
        commandManager.register(whitelistMeta, whitelistCommand)
        commandManager.register(debugMeta, debugCommand)

        if (ConfigManager.getConfig().getBoolean("features.enableVoteKick") == true) {
            commandManager.register(voteMeta, voteCommand)
            logger.info("Vote kick has been enabled. Please keep in mind that this is feature is in Beta.")
        }
    }
}