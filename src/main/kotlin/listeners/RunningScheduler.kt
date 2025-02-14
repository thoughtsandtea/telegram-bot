package dev.teaguild.thoughtsntea.listeners

import dev.inmo.kslog.common.TagLogger
import dev.inmo.kslog.common.i
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.inmo.tgbotapi.types.chat.User
import dev.starry.ktscheduler.scheduler.KtScheduler
import dev.teaguild.thoughtsntea.TastingState
import dev.teaguild.thoughtsntea.TeaTastingSession
import dev.teaguild.thoughtsntea.utils.runWeekly
import dev.teaguild.thoughtsntea.utils.sendHtml
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.plus

private val logger = TagLogger("RunningScheduler")

private object Jobs {
    const val TASTING = "Tasting"
    const val ASK = "Ask"
    const val NOTIFY = "Notify"
    const val LOCKOUT = "Lockout"
}

internal fun observeConfigToBotScheduler(session: TeaTastingSession) = with(session) {
    config
        .map { value ->
            val scheduler = KtScheduler(timeZone = value.timeZone)

            if (value.askTime >= value.tastingTime) {
                bot.sendHtml(
                    targetChatID,
                    "Inconsistent time: <code>askTime ${value.askTime}</code> is same or after <code>tastingTime ${value.tastingTime}</code>",
                )
                return@map scheduler
            }

            if (!value.botActive) return@map scheduler

            scheduler.runWeekly(jobId = Jobs.ASK, dailyTime = value.askTime, daysOfWeek = value.daysOfWeek) {
                logger.i("Asking")
                bot.send(
                    targetChatID,
                    "Good morning! Who wants to join today's tea tasting at ${value.tastingTime}? " +
                            "Use /join to register. Slots are limited to ${value.maxParticipants} participants."
                )
                check(tastingState.value == TastingState.DEFAULT && participants.value.isEmpty()) {
                    "Dirty state"
                }
                setTastingState(TastingState.ANNOUNCED)
            }

            for (reminder in value.reminders) {
                val reminderTime = value.tastingTime - reminder

                if (value.askTime < reminderTime && reminderTime < value.tastingTime) {
                    scheduler.runWeekly(
                        jobId = "${Jobs.NOTIFY} $reminder",
                        dailyTime = reminderTime,
                        daysOfWeek = value.daysOfWeek,
                    ) {
                        bot.sendHtml(
                            targetChatID,
                            "Reminder: tea tasting starts in ${reminder.toMinutes()} min! Current participants: ${
                                pingString(participants.value.values)
                            }",
                        )
                    }
                }
            }

            val lockoutTime = value.tastingTime - value.lockoutBefore

            if (value.askTime < lockoutTime && lockoutTime < value.tastingTime) {
                scheduler.runWeekly(
                    jobId = Jobs.LOCKOUT,
                    dailyTime = lockoutTime,
                    daysOfWeek = value.daysOfWeek,
                ) {
                    bot.sendHtml(
                        targetChatID, "No more registrations or cancellations allowed. Final list of participants: ${
                            pingString(participants.value.values)
                        }"
                    )
                    setTastingState(TastingState.LOCKED)
                }
            } else {
                bot.sendHtml(
                    targetChatID,
                    "Inconsistent time: <code>lockoutBefore ${value.lockoutBefore}</code> is before <code>askTime ${value.askTime}</code> or after <code>tastingTime ${value.tastingTime}</code>.",
                )
            }

            scheduler.runWeekly(jobId = Jobs.TASTING, dailyTime = value.tastingTime, daysOfWeek = value.daysOfWeek) {
                bot.send(targetChatID, "Tea tasting is now.")
                setTastingState(TastingState.DEFAULT)
            }

            return@map scheduler
        }
        .runningFold(null as KtScheduler? to null as KtScheduler?) { (_, prev), curr -> prev to curr }
        .onEach { (prev, curr) ->
            // Config may have changed, so shutting down old scheduler and emitting empty state
            prev?.shutdown()
            setTastingState(TastingState.DEFAULT)
            clearParticipants()

            curr?.start()
        }
        .launchIn(scope + CoroutineName("runningScheduler"))
}

private fun pingString(users: Collection<User>): CharSequence {
    val sb = StringBuilder()
    users.joinTo(sb) { user ->
        StringBuilder().apply {
            if (user.username != null) {
                append(user.username)
            } else {
                append("<a href=\"tg://user?id=")
                append(user.id)
                append("\">")
                append(user.firstName)
                append("</a>")
            }
        }
    }
    return sb
}
