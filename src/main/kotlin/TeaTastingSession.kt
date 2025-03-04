package club.thoughtsandtea.thoughtsntea

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.UserId
import dev.inmo.tgbotapi.types.chat.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

internal class TeaTastingSession(
    val scope: CoroutineScope,
    val bot: TelegramBot,
    val targetChatID: ChatId,
    config: TeaTastingConfig = TeaTastingConfig.Default,
    tastingState: TastingState = TastingState.DEFAULT,
) {
    private val _config: MutableStateFlow<TeaTastingConfig> = MutableStateFlow(config)
    private val _participants: MutableStateFlow<Map<UserId, User>> = MutableStateFlow(emptyMap())
    private val _tastingState: MutableStateFlow<TastingState> = MutableStateFlow(tastingState)

    val config: StateFlow<TeaTastingConfig>
        get() = _config

    val tastingState: StateFlow<TastingState>
        get() = _tastingState

    val participants: StateFlow<Map<UserId, User>>
        get() = _participants

    fun addParticipant(userId: UserId, user: User): Boolean = if (userId in _participants.value) {
        false
    } else {
        _participants.update { it + (userId to user) }
        true
    }

    fun removeParticipant(userId: UserId): Boolean = if (userId in _participants.value) {
        _participants.update { it - userId }
        true
    } else {
        false
    }

    fun clearParticipants() {
        _participants.value = emptyMap()
    }

    fun setTastingState(next: TastingState) {
        val prev = _tastingState.value
        if (prev != next) {
            check(
                when (prev) {
                    TastingState.DEFAULT -> next == TastingState.ANNOUNCED
                    TastingState.ANNOUNCED -> next == TastingState.ENOUGH || next == TastingState.LOCKED || next == TastingState.DEFAULT
                    TastingState.ENOUGH -> next == TastingState.ANNOUNCED || next == TastingState.LOCKED || next == TastingState.DEFAULT
                    TastingState.LOCKED -> next == TastingState.DEFAULT
                }
            ) { "Illegal state move from $prev to $next" }
        }
        _tastingState.value = next
    }

    inline fun updateConfig(function: (TeaTastingConfig) -> TeaTastingConfig) = _config.update(function)
}
