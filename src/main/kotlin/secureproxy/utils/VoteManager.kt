package secureproxy.utils

import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import secureproxy.Manager
import secureproxy.Manager.Companion.prefix
import secureproxy.Manager.Companion.server
import secureproxy.db.DatabaseManager
import secureproxy.extensions.broadcast
import java.util.*
import java.util.concurrent.TimeUnit

object VoteManager {

    var skipOnlinePlayerCheck = false
    var percentageOverride = 0
    var inProgress = false
    var voteYes: MutableList<String> = mutableListOf()
    var voteNo: MutableList<String> = mutableListOf()
    private val banTime = ConfigManager.getConfig().getLong("features.voteKickTimeInHours").toInt()
    private lateinit var initiatorUUID: UUID
    private lateinit var targetUUID: UUID
    var cancel = false

    fun initiateVotekick(initiator: Player, target: Player) {

        if (!skipOnlinePlayerCheck) {
            if (!checkOnlinePlayers(server)) {
                initiator.sendMessage(prefix.append(Component.text("There are", NamedTextColor.GRAY))
                    .append(Component.space())
                    .append(Component.text("not enough players online", NamedTextColor.RED))
                    .append(Component.space())
                    .append(Component.text("to start a votekick", NamedTextColor.GRAY)))
                return
            }
        }

        if (inProgress) {
            initiator.sendMessage(prefix.append(Component.text("A votekick", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("is already in progress", NamedTextColor.RED)))
            return
        }

        if (initiator == target) {
            initiator.sendMessage(prefix.append(Component.text("You", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("can't initiate", NamedTextColor.RED))
                .append(Component.space())
                .append(Component.text("a votekick on", NamedTextColor.GRAY))
                .append(Component.space())
                .append(Component.text("yourself", NamedTextColor.RED)))
            return
        }

        inProgress = true
        initiatorUUID = initiator.uniqueId
        targetUUID = target.uniqueId
        voteNo.clear()
        voteYes.clear()

        voteYes.add(initiator.username)

        server.broadcast(prefix.append(Component.text(initiator.username, NamedTextColor.GREEN))
            .append(Component.space())
            .append(Component.text("initiated a votekick to", NamedTextColor.GRAY))
            .append(Component.space())
            .append(Component.text("ban", NamedTextColor.DARK_RED))
            .append(Component.space())
            .append(Component.text("${target.username}.", NamedTextColor.RED))
            .append(Component.space())
            .append(Component.text("You now have", NamedTextColor.GRAY))
            .append(Component.space())
            .append(Component.text("5 Minutes", NamedTextColor.GREEN))
            .append(Component.space())
            .append(Component.text("to vote.", NamedTextColor.GRAY)))

        runCountdown(server, target, initiator)
    }

    private fun checkOnlinePlayers(server: ProxyServer): Boolean {
        val onlinePlayers = server.playerCount - 2

        return onlinePlayers >= 2
    }

    private fun runCountdown(server: ProxyServer, target: Player, initiator: Player) {
        var counter = 300

        Manager.scheduler
            .buildTask(Manager.instance) { self ->

                if (cancel) {
                    server.broadcast(prefix.append(Component.text("The votekick for", NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(Component.text(target.username, NamedTextColor.RED))
                        .append(Component.space())
                        .append(Component.text("has been", NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(Component.text("cancelled", NamedTextColor.DARK_RED))
                        .append(Component.space())
                        .append(Component.text("by", NamedTextColor.GRAY))
                        .append(Component.space())
                        .append(Component.text(initiator.username, NamedTextColor.GREEN)))
                    cancel = false
                    inProgress = false
                    self.cancel()
                }

                when (counter) {
                    300 -> sendAlert(server, target)
                    240 -> {
                        sendAlert(server, target)
                        sendTimeLeft(server, 4, "Minutes")
                    }
                    180 -> {
                        sendAlert(server, target)
                        sendTimeLeft(server, 3, "Minutes")
                    }
                    120 -> {
                        sendAlert(server, target)
                        sendTimeLeft(server, 2, "Minutes")
                    }
                    60 -> {
                        sendAlert(server, target)
                        sendTimeLeft(server, 1, "Minute")
                    }
                    30 -> {
                        sendAlert(server, target)
                        sendTimeLeft(server, 30, "Seconds")
                    }
                    10 -> {
                        sendAlert(server, target)
                        sendTimeLeft(server, 10, "Seconds")
                    }
                    5 -> sendTimeLeft(server, 5, "Seconds")
                    4 -> sendTimeLeft(server, 4, "Seconds")
                    3 -> sendTimeLeft(server, 3, "Seconds")
                    2 -> sendTimeLeft(server, 2, "Seconds")
                    1 -> sendTimeLeft(server, 1, "Second")
                    0 -> {

                        val requiredVotes = calculatePercentage(server.playerCount, 60.0)
                        val totalVotes = voteYes.size + voteNo.size

                        if (totalVotes < requiredVotes) {
                            server.broadcast(prefix.append(Component.text("There were", NamedTextColor.GRAY))
                                .append(Component.space())
                                .append(Component.text("not enough", NamedTextColor.RED))
                                .append(Component.space())
                                .append(Component.text("votes to", NamedTextColor.GRAY))
                                .append(Component.space())
                                .append(Component.text("kick", NamedTextColor.RED))
                                .append(Component.space())
                                .append(Component.text(target.username, NamedTextColor.GREEN)))
                            inProgress = false
                            self.cancel()
                        }

                        server.broadcast(prefix.append(Component.text("The voting for", NamedTextColor.GRAY))
                            .append(Component.space())
                            .append(Component.text(target.username, NamedTextColor.RED))
                            .append(Component.space())
                            .append(Component.text("has finished.", NamedTextColor.GREEN)))

                        server.broadcast(prefix.append(Component.text(voteYes.size, NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true))
                            .append(Component.space())
                            .append(Component.text("voted for", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                            .append(Component.space())
                            .append(Component.text("yes", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true))
                            .append(Component.text(".", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                            .append(Component.space())
                            .append(Component.text(voteNo.size, NamedTextColor.RED).decoration(TextDecoration.BOLD, true))
                            .append(Component.space())
                            .append(Component.text("voted for", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                            .append(Component.space())
                            .append(Component.text("no", NamedTextColor.RED).decoration(TextDecoration.BOLD, true))
                            .append(Component.text(".", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false)))

                        if (voteYes.size > voteNo.size) {
                            server.broadcast(prefix.append(Component.text(target.username, NamedTextColor.RED))
                                .append(Component.space())
                                .append(Component.text("will be", NamedTextColor.GRAY))
                                .append(Component.space())
                                .append(Component.text("banned for $banTime hours", NamedTextColor.RED)))

                            DatabaseManager.banPlayer(targetUUID, System.currentTimeMillis(), System.currentTimeMillis() + banTime * 60 * 60 * 1000)

                            if (server.allPlayers.contains(target)) {
                                target.disconnect(Component.text("You have been temporarily banned for $banTime Hours."))
                            }
                        } else {
                            server.broadcast(prefix.append(Component.text(target.username, NamedTextColor.GREEN))
                                .append(Component.space())
                                .append(Component.text("will", NamedTextColor.GRAY))
                                .append(Component.space())
                                .append(Component.text("not", NamedTextColor.GREEN))
                                .append(Component.space())
                                .append(Component.text("be banned.", NamedTextColor.GRAY)))
                        }

                        inProgress = false
                        self.cancel()
                    }
                }

                counter -= 1

            }
            .repeat(1L, TimeUnit.SECONDS)
            .schedule()
    }

    private fun sendAlert(server: ProxyServer, target: Player) {
        val message = prefix.append(Component.text("Ban", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)
            .append(Component.space())
            .append(Component.text(target.username, NamedTextColor.BLUE))
            .append(Component.space())
            .append(Component.text("for", NamedTextColor.GRAY))
            .append(Component.text("$banTime Hours", NamedTextColor.DARK_RED))
            .append(Component.text("?", NamedTextColor.GRAY))
            .append(Component.space())
            .append(Component.text("[Yes]", NamedTextColor.GREEN).hoverEvent(HoverEvent.showText(Component.text("Click here to vote Yes", NamedTextColor.GREEN)))
                .clickEvent(ClickEvent.runCommand("/vote yes")))
            .append(Component.space())
            .append(Component.text("[No]", NamedTextColor.RED).hoverEvent(HoverEvent.showText(Component.text("Click here to vote No", NamedTextColor.RED)))
                .clickEvent(ClickEvent.runCommand("/vote no"))))

        server.broadcast(message)
    }

    private fun sendTimeLeft(server: ProxyServer, time: Int, unit: String) {
        val message = prefix.append(Component.text("Only", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false)
            .append(Component.space())
            .append(Component.text("$time $unit", NamedTextColor.RED))
            .append(Component.space())
            .append(Component.text("remaining.", NamedTextColor.GRAY)))

        server.broadcast(message)
    }

    fun vote(player: Player, decision: Boolean) {

        if (!inProgress) {
            player.sendMessage(prefix.append(Component.text("There is no current votekick.", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)))
            return
        }

        if (voteYes.contains(player.username) || voteNo.contains(player.username)) {
            player.sendMessage(prefix.append(Component.text("You have already voted. Your decision is final.", NamedTextColor.DARK_RED).decoration(TextDecoration.BOLD, false)))
            return
        }

        if (player.uniqueId == targetUUID) {
            player.sendMessage(prefix.append(Component.text("You are not allowed to vote for yourself.", NamedTextColor.RED).decoration(TextDecoration.BOLD, false)))
            return
        }

        if (decision) {
            voteYes.add(player.username)
        } else {
            voteNo.add(player.username)
        }
    }
}

fun calculatePercentage(value: Int, percentage: Double): Int {
    if (VoteManager.percentageOverride > 0) {
        return VoteManager.percentageOverride
    }
    return ((value * percentage) / 100).toInt()
}