package dev.teaguild.thoughtsntea.listeners

import dev.inmo.tgbotapi.extensions.api.send.send
import dev.teaguild.thoughtsntea.TastingState
import dev.teaguild.thoughtsntea.TeaTastingSession
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
                        bot.send(targetChatID, "Enough participants are registered (${value.size}).")
                    }

                    value.size < config.value.maxParticipants -> {
                        val free = config.value.maxParticipants - value.size
                        if (free == 1) {
                            bot.send(targetChatID, "There is 1 free place.")
                        } else {
                            bot.send(targetChatID, "There are $free free places.")
                        }
                    }
                }
        }
        .launchIn(session.scope + CoroutineName("participantsCount"))
}
