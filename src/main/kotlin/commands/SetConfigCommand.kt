package dev.teaguild.thoughtsntea.commands

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.message.HTML
import dev.teaguild.thoughtsntea.TeaTastingSession
import dev.teaguild.thoughtsntea.utils.emptyEnumSet
import dev.teaguild.thoughtsntea.utils.inGroupChat
import dev.teaguild.thoughtsntea.utils.isFromAdministratorUser
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

internal suspend fun BehaviourContext.setConfigCommand(
    session: TeaTastingSession,
) = onCommand("setconfig".toRegex(), requireOnlyCommandInMessage = false) { message ->
    with(session) {
        if (!message.inGroupChat(targetChatID)) return@onCommand
        if (!message.isFromAdministratorUser(bot, targetChatID)) return@onCommand

        val args = message.content.text.split(" ", limit = 3)

        if (args.size != 3) {
            reply(message, "Missing arguments. Usage: /setconfig <property> <value>.")
            return@onCommand
        }

        val property = args[1]
        val text = args[2]

        when (property) {
            "daysOfWeek" -> {
                DayOfWeek.entries
                try {
                    val days = text.split(",")
                        .map { it.trim().uppercase() }
                        .mapTo(emptyEnumSet()) { DayOfWeek.valueOf(it) }
                    updateConfig { prev -> prev.copy(daysOfWeek = days) }
                } catch (_: IllegalArgumentException) {
                    //language=HTML
                    reply(
                        message,
                        "Invalid day(s) of the week. Please use valid day names (e.g., <code>MONDAY</code>, <code>TUESDAY</code>).",
                        parseMode = HTML,
                    )
                    return@onCommand
                }
            }

            "askTime", "tastingTime" -> {
                try {
                    val parsedTime = LocalTime.parse(text)
                    updateConfig { prev ->
                        if (property == "askTime") prev.copy(askTime = parsedTime)
                        else prev.copy(tastingTime = parsedTime)
                    }
                } catch (_: DateTimeParseException) {
                    //language=HTML
                    reply(
                        message,
                        "Invalid time format. Please use HH:mm (e.g., <code>14:30</code>).",
                        parseMode = HTML,
                    )
                    return@onCommand
                }
            }

            "maxParticipants" -> {
                val maxParticipants = text.toIntOrNull()
                if (maxParticipants == null || maxParticipants <= 0) {
                    reply(message, "Invalid number for max participants.")
                    return@onCommand
                }
                updateConfig { prev -> prev.copy(maxParticipants = maxParticipants) }
            }

            "reminders" -> {
                val reminders = text.split(",").mapNotNull { it.trim().toLongOrNull() }
                if (reminders.isEmpty()) {
                    reply(message, "Invalid format for reminders. Example: 30,10")
                    return@onCommand
                }
                updateConfig { prev -> prev.copy(remindersMinutes = reminders) }
            }

            "lockoutBefore" -> {
                val lockout = text.toLongOrNull()
                if (lockout == null || lockout <= 0) {
                    reply(message, "Invalid number for lockout period.")
                    return@onCommand
                }
                updateConfig { prev -> prev.copy(lockoutBeforeMinutes = lockout) }
            }

            "botActive" -> {
                val isActive = text.lowercase().toBooleanStrictOrNull()
                if (isActive == null) {
                    //language=HTML
                    reply(
                        message,
                        "Invalid value for bot active. Use <code>true</code> or <code>false</code>.",
                        parseMode = HTML
                    )
                    return@onCommand
                }
                updateConfig { prev -> prev.copy(botActive = isActive) }
            }

            "timeZone" -> {
                try {
                    val timeZone = ZoneId.of(text)
                    updateConfig { prev -> prev.copy(timeZone = timeZone) }
                } catch (_: Exception) {
                    reply(message, "Invalid time zone: $text")
                    return@onCommand
                }
            }

            else -> {
                reply(message, "Unknown property: $property")
                return@onCommand
            }
        }


        //language=HTML
        reply(message, "Configuration updated: <code>$property</code> → <code>$text</code>", parseMode = HTML)
    }
}
