package secureproxy.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import secureproxy.Manager.Companion.prefix
import secureproxy.utils.VoteManager

val debugHelpMessage =
    prefix.append(Component.text("/sd <set | get | list> [variable] [value]", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))

val debugVariableSets: List<String> = listOf("skipOnlinePlayerCheck", "percentageOverride", "inProgress", "voteYes", "voteNo")

object DebugCommand {
    fun createBrigadierCommand(): BrigadierCommand {
        val debugNode =
            BrigadierCommand.literalArgumentBuilder("sd")
                .requires { source -> source.hasPermission("secureproxy.debug") }
                .executes { context: CommandContext<CommandSource> ->
                    val source = context.source

                    source.sendMessage(debugHelpMessage)
                    Command.SINGLE_SUCCESS
                }
                .then(BrigadierCommand.literalArgumentBuilder("list")
                    .executes { context: CommandContext<CommandSource> ->
                        val listMessage = prefix.append(Component.text("Available variable sets: ", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false)
                            .append(Component.text(debugVariableSets.toString())))

                        context.source.sendMessage(listMessage)
                        Command.SINGLE_SUCCESS
                    })
                .then(BrigadierCommand.literalArgumentBuilder("set")
                    .then(BrigadierCommand.requiredArgumentBuilder(
                        "variable",
                        StringArgumentType.word()
                    )
                        .suggests { _: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                            debugVariableSets.forEach {
                                builder.suggest(it)
                            }

                            builder.buildFuture()
                        }
                        .then(BrigadierCommand.requiredArgumentBuilder(
                            "value",
                            StringArgumentType.word()
                        )
                            .executes { context: CommandContext<CommandSource> ->
                                val providedVariable = context.getArgument("variable", String::class.java)
                                val providedValue = context.getArgument("value", String::class.java)

                                when (providedVariable) {
                                    "skipOnlinePlayerCheck" -> VoteManager.skipOnlinePlayerCheck = providedValue != "0"
                                    "percentageOverride" -> VoteManager.percentageOverride += 1
                                    "inProgress" -> VoteManager.inProgress = providedValue != "0"
                                    "voteYes" -> {
                                        if (providedValue == "clear") { VoteManager.voteYes.clear() }
                                        VoteManager.voteYes.add(providedValue)
                                    }
                                    "voteNo" -> {
                                        if (providedValue == "clear") { VoteManager.voteNo.clear() }
                                        VoteManager.voteNo.add(providedValue)
                                    }
                                }

                                Command.SINGLE_SUCCESS
                            })))
                .then(BrigadierCommand.literalArgumentBuilder("get")
                    .then(BrigadierCommand.requiredArgumentBuilder(
                        "variable",
                        StringArgumentType.word()
                    )
                        .suggests { _: CommandContext<CommandSource>, builder: SuggestionsBuilder ->
                            debugVariableSets.forEach {
                                builder.suggest(it)
                            }

                            builder.buildFuture()
                        }
                        .executes { context: CommandContext<CommandSource> ->
                            val providedVariable = context.getArgument("variable", String::class.java)

                            when (providedVariable) {
                                "skipOnlinePlayerCheck" -> context.source.sendMessage(prefix.append(Component.text(VoteManager.skipOnlinePlayerCheck.toString())))
                                "percentageOverride" -> context.source.sendMessage(prefix.append(Component.text(VoteManager.percentageOverride.toString())))
                                "inProgress" -> context.source.sendMessage(prefix.append(Component.text(VoteManager.inProgress.toString())))
                                "voteYes" -> context.source.sendMessage(prefix.append(Component.text(VoteManager.voteYes.toString())))
                                "voteNo" -> context.source.sendMessage(prefix.append(Component.text(VoteManager.voteNo.toString())))
                            }

                            Command.SINGLE_SUCCESS
                        }))
        return BrigadierCommand(debugNode)
    }
}