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
import secureproxy.Manager.Companion.prefix
import secureproxy.db.DatabaseManager
import java.util.function.Consumer

val helpMessage =
    prefix.append(Component.text("/sw <list | add | remove> [player] [desiredMode = online | offline]", NamedTextColor.GRAY))

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
                        val listMessage = prefix.append(Component.text("The following players are currently on the Whitelist: ", NamedTextColor.GRAY)
                            .append(Component.text(DatabaseManager.getWhitelistedPlayers().toString(), NamedTextColor.GREEN)))

                        context.source.sendMessage(listMessage)
                        Command.SINGLE_SUCCESS
                    })
                .then(BrigadierCommand.literalArgumentBuilder("add")
                    .then(BrigadierCommand.requiredArgumentBuilder(
                        "player",
                        StringArgumentType.word()
                    )
                        .suggests { _: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                            proxy.allPlayers.forEach(Consumer { player: Player ->
                                builder.suggest(player.username)
                            })

                            builder.buildFuture()
                        }
                        .then(BrigadierCommand.requiredArgumentBuilder(
                            "mode",
                            StringArgumentType.word()
                        )
                            .suggests { _: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                                builder.suggest("online")
                                builder.suggest("offline")

                                builder.buildFuture()
                            }
                            .executes { context: CommandContext<CommandSource> ->
                                val providedPlayer = context.getArgument("player", String::class.java)
                                val providedMode = context.getArgument("mode", String::class.java).lowercase()
                                var mode = true

                                if (DatabaseManager.getWhitelist(providedPlayer)) {
                                    val alreadyExistsMessage = prefix.append(Component.text("The Player", NamedTextColor.GRAY))
                                        .append(Component.space())
                                        .append(Component.text(providedPlayer, NamedTextColor.GREEN))
                                        .append(Component.space())
                                        .append(Component.text("is already", NamedTextColor.RED))
                                        .append(Component.text("whitelisted.", NamedTextColor.GRAY))
                                    context.source.sendMessage(alreadyExistsMessage)
                                    Command.SINGLE_SUCCESS
                                } else {
                                    if (providedMode == "offline") {
                                        mode = false
                                    }

                                    DatabaseManager.addWhitelist(providedPlayer, mode)

                                    val successMessage = prefix.append(Component.text("The Player", NamedTextColor.GRAY))
                                        .append(Component.space())
                                        .append(Component.text(providedPlayer, NamedTextColor.GREEN))
                                        .append(Component.space())
                                        .append(Component.text("has been", NamedTextColor.DARK_GREEN))
                                        .append(Component.space())
                                        .append(Component.text("whitelisted.", NamedTextColor.GRAY))
                                    context.source.sendMessage(successMessage)

                                    Command.SINGLE_SUCCESS
                                }
                            })))
                .then(BrigadierCommand.literalArgumentBuilder("remove")
                    .then(BrigadierCommand.requiredArgumentBuilder(
                        "player",
                        StringArgumentType.word()
                    )
                        .suggests { _: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                            proxy.allPlayers.forEach(Consumer { player: Player ->
                                builder.suggest(player.username)
                            })

                            builder.buildFuture()
                        }
                        .executes { context: CommandContext<CommandSource> ->
                            val providedPlayer = context.getArgument("player", String::class.java)

                            if (!DatabaseManager.getWhitelist(providedPlayer)) {
                                val dontExistsMessage = prefix.append(Component.text("The Player", NamedTextColor.GRAY))
                                    .append(Component.space())
                                    .append(Component.text(providedPlayer, NamedTextColor.GREEN))
                                    .append(Component.space())
                                    .append(Component.text("is not", NamedTextColor.RED))
                                    .append(Component.space())
                                    .append(Component.text("whitelisted.", NamedTextColor.GRAY))
                                context.source.sendMessage(dontExistsMessage)
                                Command.SINGLE_SUCCESS
                            }

                            DatabaseManager.removeWhitelist(providedPlayer)

                            if (DatabaseManager.getUser(providedPlayer)) {
                                DatabaseManager.deleteUser(providedPlayer)
                            }

                            val successMessage = prefix.append(Component.text("The Player", NamedTextColor.GRAY))
                                .append(Component.space())
                                .append(Component.text(providedPlayer, NamedTextColor.GREEN))
                                .append(Component.space())
                                .append(Component.text("has been removed", NamedTextColor.RED))
                                .append(Component.space())
                                .append(Component.text("from the whitelist.", NamedTextColor.GRAY))
                            context.source.sendMessage(successMessage)

                            Command.SINGLE_SUCCESS
                        }))
        return BrigadierCommand(whitelistNode)
    }
}
