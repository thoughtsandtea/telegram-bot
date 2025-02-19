package dev.teaguild.thoughtsntea.commands

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.teaguild.thoughtsntea.TeaTastingSession
import dev.teaguild.thoughtsntea.utils.isFromAdministratorUser
import dev.teaguild.thoughtsntea.utils.replyHtml
import java.time.format.TextStyle
import java.util.*

internal suspend fun BehaviourContext.showConfigCommand(
    session: TeaTastingSession,
) = onCommand("showconfig") { message ->
    with(session) {
        if (!message.isFromAdministratorUser(bot, targetChatID)) return@onCommand

        with(config.value) {
            replyHtml(
                message, """
                        <b>Current bot configuration</b>:
                         - <code>daysOfWeek</code>: <code>${
                    daysOfWeek.joinToString(",") { d -> d.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
                }</code>
                         - <code>askTime</code>: <code>$askTime</code>
                         - <code>maxParticipants</code>: <code>$maxParticipants</code>
                         - <code>tastingTime</code>: <code>$tastingTime</code>
                         - <code>reminders</code> before tasting: <code>${remindersMinutes.joinToString(",")}</code> min
                         - <code>lockoutBefore</code> before tasting: <code>$lockoutBeforeMinutes</code> min
                         - <code>botActive</code>: ${if (botActive) "<code>true</code>" else "<code>false</code>"}
                         - <code>timeZone</code>: <code>${timeZone.id}</code>""".trimIndent(),
            )
        }
    }
}
