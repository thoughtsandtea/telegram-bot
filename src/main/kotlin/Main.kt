package dev.teaguild.thoughtsntea

import dev.inmo.kslog.common.TagLogger
import dev.inmo.kslog.common.i
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.send.reply
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.toChatId
import dev.teaguild.thoughtsntea.commands.setConfigCommand
import dev.teaguild.thoughtsntea.commands.showConfigCommand
import dev.teaguild.thoughtsntea.listeners.observeConfigToBotScheduler
import dev.teaguild.thoughtsntea.listeners.observeConfigToSave
import dev.teaguild.thoughtsntea.listeners.observeParticipantsCount
import dev.teaguild.thoughtsntea.listeners.observeSessionToLog
import dev.teaguild.thoughtsntea.utils.getenvOrFail
import dev.teaguild.thoughtsntea.utils.inGroupChat
import dev.teaguild.thoughtsntea.utils.isFromAdministratorUser
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

    observeConfigToBotScheduler(session)
    observeConfigToSave(session)
    observeSessionToLog(session)
    observeParticipantsCount(session)

    session.bot.buildBehaviourWithLongPolling {
        val me = getMe()
        logger.i(me)

        // Admin commands

        showConfigCommand(session)
        setConfigCommand(session)

        onCommand("help") { message ->
            if (!message.isFromAdministratorUser(bot, session.targetChatID)) return@onCommand
            reply(message, """""")
        }

//        onCommand("cancelToday") { message ->
//            if (!message.inGroupChat(session.targetChatID)) return@onCommand
//        }

        // User commands

        onCommand("start") { message ->
            reply(message, """""")
        }

        onCommand("join") { message ->
            if (!message.inGroupChat(session.targetChatID)) return@onCommand
            val from = message.fromUserOrNull()?.from ?: return@onCommand
            val fromId = from.id

            if (session.tastingState.value != TastingState.ANNOUNCED) {
                reply(message, "New participants are not being registered now.")
                return@onCommand
            }

            if (session.tastingState.value == TastingState.DEFAULT) return@onCommand
            if (!session.addParticipant(fromId, from))
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
