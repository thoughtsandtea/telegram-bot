package dev.teaguild.thoughtsntea

import dev.inmo.kslog.common.TagLogger
import dev.inmo.kslog.common.i
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.toChatId
import dev.teaguild.thoughtsntea.commands.setConfigCommand
import dev.teaguild.thoughtsntea.commands.showConfigCommand
import dev.teaguild.thoughtsntea.listeners.observeConfigToBotScheduler
import dev.teaguild.thoughtsntea.listeners.observeConfigToSave
import dev.teaguild.thoughtsntea.listeners.observeParticipantsCount
import kotlinx.coroutines.*
import java.util.*

private val logger = TagLogger("Main")

/**
 * Bot state machine.
 *
 * [DEFAULT] -> [ANNOUNCED] <-> [ENOUGH] -> [LOCKED] -> [DEFAULT]
 */
enum class TastingState {
    DEFAULT,
    ANNOUNCED,
    ENOUGH,
    LOCKED,
}

fun main() = runBlocking {
    Locale.setDefault(Locale.US)

    val session = run {
        val token = getenvOrFail("THOUGHTSNTEA_TELEGRAM_BOT_TOKEN")
        val targetChatID = getenvOrFail("THOUGHTSNTEA_TELEGRAM_CHAT_ID").toLong().toChatId()
        val bot = telegramBot(token)
        TeaTastingSession(this, bot, targetChatID, loadConfig())
    }

    logger.i("Loaded config: ${session.config.value}")

    observeConfigToBotScheduler(session)
    observeConfigToSave(session)
    observeParticipantsCount(session)

    session.bot.buildBehaviourWithLongPolling {
        val me = getMe()
        logger.i(me)

        // Admin commands
        showConfigCommand(session)
        setConfigCommand(session)

        onCommand("help") { message ->
            if (message.chat !is GroupChat || message.chat.id != session.targetChatID) return@onCommand
            reply(message, """""")
        }

        // User commands

        onCommand("join") { message ->
            if (!message.inGroupChat(session.targetChatID)) return@onCommand
            if (session.tastingState.value != TastingState.ANNOUNCED) {
                reply(message, "New participants are not being registered now.")
                return@onCommand
            }
            if (session.tastingState.value == TastingState.DEFAULT) return@onCommand
            val userId = message.fromUserOrNull()?.from?.id ?: return@onCommand
            if (!session.addParticipant(userId))
                reply(message, "You are already registered.")
        }

        onCommand("leave") { message ->
            if (!message.inGroupChat(session.targetChatID)) return@onCommand
            if (session.tastingState.value != TastingState.ENOUGH || session.tastingState.value != TastingState.ANNOUNCED) {
                return@onCommand
            }
            val userId = message.fromUserOrNull()?.from?.id ?: return@onCommand
            session.removeParticipant(userId)
        }

    }.join()
}
