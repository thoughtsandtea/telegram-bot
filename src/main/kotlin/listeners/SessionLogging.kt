package dev.teaguild.thoughtsntea.listeners

import dev.inmo.kslog.common.TagLogger
import dev.inmo.kslog.common.i
import dev.teaguild.thoughtsntea.TeaTastingSession
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private val logger = TagLogger("SessionLogging")

internal fun observeSessionToLog(session: TeaTastingSession) = with(session) {
    config
        .onEach { value ->
            logger.i { "Config: $value" }
        }
        .launchIn(scope)

    participants
        .onEach { value ->
            logger.i { "Participants: $value" }
        }
        .launchIn(scope)

    tastingState
        .onEach { value ->
            logger.i { "State: $value" }
        }
        .launchIn(scope)
}
