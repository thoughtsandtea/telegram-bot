package dev.teaguild.thoughtsntea.listeners

import dev.inmo.kslog.common.TagLogger
import dev.inmo.kslog.common.i
import dev.inmo.tgbotapi.extensions.api.send.send
import dev.starry.ktscheduler.scheduler.KtScheduler
import dev.teaguild.thoughtsntea.TastingState
import dev.teaguild.thoughtsntea.TeaTastingSession
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
            if (!value.botActive) return@map scheduler

            // TODO do custom trigger for days of weeks probably

            scheduler.runDaily(Jobs.ASK, value.askTime) {
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
                scheduler.runDaily("${Jobs.NOTIFY} $reminder", value.tastingTime - reminder) {
                    logger.i("Reminding $reminder before")
                    bot.send(targetChatID, "1")
                }
            }

            scheduler.runDaily(Jobs.LOCKOUT, value.askTime){
                logger.i("Lockout")
                bot.send(targetChatID, "Lockout")
            }

            scheduler.runDaily(Jobs.TASTING, value.tastingTime) {
                logger.i("Tasting")
                bot.send(targetChatID, "Tasting")
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
