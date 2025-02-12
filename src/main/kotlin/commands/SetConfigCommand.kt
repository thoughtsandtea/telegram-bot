package dev.teaguild.thoughtsntea.commands

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.teaguild.thoughtsntea.TeaTastingSession
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId

internal suspend fun BehaviourContext.setConfigCommand(session: TeaTastingSession) = with(session) {
    onCommand("setconfig".toRegex(), requireOnlyCommandInMessage = false) { message ->
        if (message.chat !is GroupChat || message.chat.id != targetChatID) return@onCommand

        val args = message.content.text.split(" ", limit = 3)

        if (args.size != 3) {
            reply(message, "Missing arguments. Usage: /setconfig <property> <value>")
            return@onCommand
        }

        val property = args[1]
        val text = args[2]

        updateConfig { prev ->
            when (property) {
                "days_of_week" -> {
                    val days = text.split(",").map { it.trim().uppercase() }.map { DayOfWeek.valueOf(it) }
                    prev.copy(daysOfWeek = days)
                }

                "ask_time" -> prev.copy(askTime = LocalTime.parse(text))
                "tasting_time" -> prev.copy(tastingTime = LocalTime.parse(text))

                "max_participants" -> {
                    val maxParticipants = text.toIntOrNull()
                    if (maxParticipants == null || maxParticipants <= 0) {
                        reply(message, "Invalid number for max participants")
                        return@onCommand
                    }
                    prev.copy(maxParticipants = maxParticipants)
                }

                "reminders" -> {
                    val reminders = text.split(",").mapNotNull { it.trim().toLongOrNull() }
                    if (reminders.isEmpty()) {
                        reply(message, "Invalid format for reminders. Example: 30,10")
                        return@onCommand
                    }
                    prev.copy(remindersMinutes = reminders)
                }

                "lockout_before" -> {
                    val lockout = text.toLongOrNull()
                    if (lockout == null || lockout <= 0) {
                        reply(message, "Invalid number for lockout period")
                        return@onCommand
                    }
                    prev.copy(lockoutBeforeMinutes = lockout)
                }

                "bot_active" -> {
                    val isActive = text.lowercase().toBooleanStrictOrNull()
                    if (isActive == null) {
                        reply(message, "Invalid value for bot_active. Use true or false.")
                        return@onCommand
                    }
                    prev.copy(botActive = isActive)
                }

                "time_zone" -> {
                    try {
                        val timeZone = ZoneId.of(text)
                        prev.copy(timeZone = timeZone)
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
        }

        reply(message, "Configuration updated: $property -> $text")
    }
}
