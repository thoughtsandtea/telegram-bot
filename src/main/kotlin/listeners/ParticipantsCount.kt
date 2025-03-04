package club.thoughtsandtea.thoughtsntea.listeners

import dev.inmo.tgbotapi.extensions.api.send.send
import club.thoughtsandtea.thoughtsntea.TastingState
import club.thoughtsandtea.thoughtsntea.TeaTastingSession
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus

internal fun observeParticipantsCount(session: TeaTastingSession) = with(session) {
    participants
        .onEach { value ->
            if (tastingState.value == TastingState.ANNOUNCED)
                when {
                    value.size > config.value.maxParticipants -> error("Illegally many participants.")

                    value.size == config.value.maxParticipants -> {
                        setTastingState(TastingState.ENOUGH)
                        bot.send(
                            targetChatID,
                            "🎉 Perfect! We have all ${value.size} participants registered for today's tea tasting!"
                        )
                    }

                    value.size < config.value.maxParticipants -> {
                        if (tastingState.value == TastingState.ENOUGH) setTastingState(TastingState.ANNOUNCED)
                        val free = config.value.maxParticipants - value.size
                        if (free == 1) {
                            bot.send(targetChatID, "1\uFE0F⃣ Only one spot remaining!")
                        } else {
                            bot.send(targetChatID, "\uD83D\uDC65 $free spots still available! Join us for tea!")
                        }
                    }
                }
        }
        .launchIn(session.scope + CoroutineName("participantsCount"))
}
