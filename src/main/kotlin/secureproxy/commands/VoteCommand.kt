package secureproxy.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import secureproxy.Manager.Companion.prefix
import secureproxy.utils.VoteManager
import java.util.function.Consumer

val voteHelpMessage = prefix.append(Component.text("/vote <player | yes | no | cancel>", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))

object VoteCommand {
    fun createBrigadierCommand(proxy: ProxyServer): BrigadierCommand {
        val voteNode =
            BrigadierCommand.literalArgumentBuilder("vote")
                .then(BrigadierCommand.requiredArgumentBuilder(
                    "action",
                    StringArgumentType.word()
                )
                    .suggests { _: CommandContext<CommandSource>, builder: SuggestionsBuilder ->

                        builder.suggest("yes")
                        builder.suggest("no")
                        builder.suggest("cancel")

                        proxy.allPlayers.forEach(Consumer { player: Player ->
                            builder.suggest(player.username)
                        })

                        builder.buildFuture()
                    }
                    .executes { context: CommandContext<CommandSource> ->
                        val providedAction = context.getArgument("action", String::class.java)
                        val player = context.source as Player

                        when (providedAction) {
                            "yes" -> VoteManager.vote(player, true)
                            "no" -> VoteManager.vote(player, false)
                            "cancel" -> VoteManager.cancel = true
                            else -> {
                                VoteManager.initiateVotekick(player, proxy.getPlayer(providedAction).get())
                            }
                        }

                        Command.SINGLE_SUCCESS
                    })
                .requires { source -> source.hasPermission("secureproxy.votekick") }
                .executes { context: CommandContext<CommandSource> ->
                    val source = context.source

                    source.sendMessage(voteHelpMessage)
                    Command.SINGLE_SUCCESS
                }
                .build()

        return BrigadierCommand(voteNode)
    }
}