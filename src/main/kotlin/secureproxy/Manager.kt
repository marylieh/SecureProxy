package secureproxy

import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.proxy.ProxyServer
import java.util.logging.Logger
import javax.inject.Inject

@Plugin(
    id = "secureproxy",
    name = "secure-proxy",
    version = "1.0.0",
    description = "A simple secure proxy which allows non premium players to join but do not allow impersonating other players.",
    url = "https://marylieh.social/",
    authors = ["marylieh"]
)
class Manager {

    private lateinit var server: ProxyServer
    private lateinit var logger: Logger

    @Inject
    fun velocityStartup(server: ProxyServer, logger: Logger) {
        this.server = server
        this.logger = logger

        logger.info("Secure Proxy started.")
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {

    }
}