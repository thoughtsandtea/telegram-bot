package dev.teaguild.thoughtsntea.commands

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.teaguild.thoughtsntea.TeaTastingSession
import java.time.format.TextStyle
import java.util.*

internal suspend fun BehaviourContext.showConfigCommand(session: TeaTastingSession) =
    onCommand("showconfig") { message ->
        with(session) {
            if (message.chat is GroupChat && message.chat.id != targetChatID) return@onCommand

            with(config.value) {
                reply(
                    message, """
                        Current bot configuration:
                         - Days of week: ${
                        daysOfWeek.joinToString(", ") { d -> d.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
                    }
                         - Ask time: ${askTime}
                         - Max participants: ${maxParticipants}
                         - Tasting time: ${tastingTime}
                         - Reminders before tasting: ${remindersMinutes.joinToString(", ")} min
                         - Lockout time before tasting: ${lockoutBeforeMinutes} min
                         - Bot active: ${if (botActive) "Yes" else "No"}
                         - Time zone: ${timeZone.id}""".trimIndent()
                )
            }
        }
    }
