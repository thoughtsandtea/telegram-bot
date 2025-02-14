package dev.teaguild.thoughtsntea.listeners

import dev.teaguild.thoughtsntea.TeaTastingSession
import dev.teaguild.thoughtsntea.saveConfig
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

internal fun observeConfigToSave(session: TeaTastingSession) = session.config
    .onEach { value ->
        withContext(Dispatchers.IO) { saveConfig(value) }
    }
    .launchIn(session.scope + CoroutineName("savingConfig"))
