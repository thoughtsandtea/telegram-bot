package dev.teaguild.thoughtsntea

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

internal fun observeConfigToSave(session: TeaTastingSession) {
    session.config
        .onEach {
            withContext(Dispatchers.IO) { saveConfig(it) }
        }
        .launchIn(session.scope + CoroutineName("savingConfig"))
}
