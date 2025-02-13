package dev.teaguild.thoughtsntea.listeners

import dev.inmo.kslog.common.TagLogger
import dev.inmo.kslog.common.i
import dev.teaguild.thoughtsntea.TeaTastingSession
import dev.teaguild.thoughtsntea.saveConfig
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

private val logger = TagLogger("SavingConfig")

internal fun observeConfigToSave(session: TeaTastingSession) {
    session.config
        .onEach { value ->
            logger.i { value }
            withContext(Dispatchers.IO) { saveConfig(value) }
        }
        .launchIn(session.scope + CoroutineName("savingConfig"))
}
