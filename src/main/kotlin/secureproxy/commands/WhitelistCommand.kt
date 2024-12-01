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
import secureproxy.db.DatabaseManager
import java.util.function.Consumer

val helpMessage =
    prefix.append(Component.text("/sw <list | add | remove> [player] [desiredMode = online | offline]", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))

object WhitelistCommand {
    fun createBrigadierCommand(proxy: ProxyServer): BrigadierCommand {
        val whitelistNode =
            BrigadierCommand.literalArgumentBuilder("sw")
                .requires { source -> source.hasPermission("secureproxy.whitelist") }
                .executes { context: CommandContext<CommandSource> ->
                    val source = context.source

                    source.sendMessage(helpMessage)
                    Command.SINGLE_SUCCESS
                }
                .then(BrigadierCommand.literalArgumentBuilder("list")
                    .executes { context: CommandContext<CommandSource> ->
                        val listMessage = prefix.append(Component.text("The following players are currently on the Whitelist: ", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false)
                            .append(Component.text(DatabaseManager.getWhitelistedPlayers().toString())))

                        context.source.sendMessage(listMessage)
                        Command.SINGLE_SUCCESS
                    })
                .then(BrigadierCommand.literalArgumentBuilder("add")
                    .then(BrigadierCommand.requiredArgumentBuilder(
                        "player",
                        StringArgumentType.word()
                    )
                        .suggests { ctx: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                            proxy.allPlayers.forEach(Consumer { player: Player ->
                                builder.suggest(player.username)
                            })

                            builder.buildFuture()
                        }
                        .then(BrigadierCommand.requiredArgumentBuilder(
                            "mode",
                            StringArgumentType.word()
                        )
                            .suggests { ctx: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                                builder.suggest("online")
                                builder.suggest("offline")

                                builder.buildFuture()
                            }
                            .executes { context: CommandContext<CommandSource> ->
                                val providedPlayer = context.getArgument("player", String::class.java)
                                val providedMode = context.getArgument("mode", String::class.java).lowercase()
                                var mode = true

                                if (DatabaseManager.getWhitelist(providedPlayer)) {
                                    val alreadyExistsMessage = prefix.append(Component.text("The Player $providedPlayer is already on the Whitelist.", NamedTextColor.RED).decoration(TextDecoration.BOLD, false))
                                    context.source.sendMessage(alreadyExistsMessage)
                                    Command.SINGLE_SUCCESS
                                }

                                if (providedMode == "offline") {
                                    mode = false
                                }

                                DatabaseManager.addWhitelist(providedPlayer, mode)

                                val successMessage = prefix.append(Component.text("The Player $providedPlayer has been added to the Whitelist.", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false))
                                context.source.sendMessage(successMessage)

                                Command.SINGLE_SUCCESS
                            })))
                .then(BrigadierCommand.literalArgumentBuilder("remove")
                    .then(BrigadierCommand.requiredArgumentBuilder(
                        "player",
                        StringArgumentType.word()
                    )
                        .suggests { ctx: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                            proxy.allPlayers.forEach(Consumer { player: Player ->
                                builder.suggest(player.username)
                            })

                            builder.buildFuture()
                        }
                        .executes { context: CommandContext<CommandSource> ->
                            val providedPlayer = context.getArgument("player", String::class.java)

                            if (!DatabaseManager.getWhitelist(providedPlayer)) {
                                val dontExistsMessage = prefix.append(Component.text("The Player $providedPlayer is not on the whitelist.", NamedTextColor.RED).decoration(TextDecoration.BOLD, false))
                                context.source.sendMessage(dontExistsMessage)
                                Command.SINGLE_SUCCESS
                            }

                            DatabaseManager.removeWhitelist(providedPlayer)

                            val successMessage = prefix.append(Component.text("The Player $providedPlayer has been removed from the whitelist.", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, false))
                            context.source.sendMessage(successMessage)

                            Command.SINGLE_SUCCESS
                        }))
        return BrigadierCommand(whitelistNode)
    }
}
