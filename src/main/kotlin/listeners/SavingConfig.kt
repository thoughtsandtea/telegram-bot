package club.thoughtsandtea.thoughtsntea.listeners

import club.thoughtsandtea.thoughtsntea.TeaTastingSession
import club.thoughtsandtea.thoughtsntea.saveConfig
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
