package secureproxy.extensions

import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component

fun ProxyServer.broadcast(message: Component) {
    this.allPlayers.forEach {
        it.sendMessage(message)
    }
}