package dev.teaguild.thoughtsntea.commands

import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.types.message.HTML
import dev.teaguild.thoughtsntea.TeaTastingSession
import dev.teaguild.thoughtsntea.inGroupChat
import dev.teaguild.thoughtsntea.isFromAdministratorUser
import java.time.format.TextStyle
import java.util.*

internal suspend fun BehaviourContext.showConfigCommand(
    session: TeaTastingSession,
) = onCommand("showconfig") { message ->
    with(session) {
        if (!message.inGroupChat(targetChatID)) return@onCommand
        if (!message.isFromAdministratorUser(bot, targetChatID)) return@onCommand

        with(config.value) {
            //language=html
            reply(
                message, """
                        Current bot configuration:
                         - <code>daysOfWeek</code>: <code>${
                    daysOfWeek.joinToString(", ") { d -> d.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
                }</code>
                         - <code>askTime</code>: <code>$askTime</code>
                         - <code>maxParticipants</code>: <code>$maxParticipants</code>
                         - <code>tastingTime</code>: <code>$tastingTime</code>
                         - <code>reminders</code> before tasting: <code>${remindersMinutes.joinToString(",")}</code> min
                         - <code>lockoutBefore</code> before tasting: <code>$lockoutBeforeMinutes</code> min
                         - <code>botActive</code>: ${if (botActive) "<code>true</code>" else "<code>false</code>"}
                         - <code>timeZone</code>: <code>${timeZone.id}</code>""".trimIndent(),
                parseMode = HTML,
            )
        }
    }
}
