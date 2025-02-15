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
import dev.teaguild.thoughtsntea.utils.replyHtml
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
            replyHtml(
                message,
                """
        <b>Admin Commands:</b>
        /help - Show this help message
        /showconfig - Display current bot configuration
        
        /setconfig - Modify bot configuration.
        Usage: /setconfig &lt;property&gt; &lt;value&gt;
          Properties:
          • <code>daysOfWeek</code> - Days when tasting occurs (e.g., <code>monday,wednesday</code>)
          • <code>askTime</code> - Time when bot asks for participants (HH:mm)
          • <code>tastingTime</code> - Time when tasting starts (HH:mm)
          • <code>maxParticipants</code> - Maximum number of participants allowed
          • <code>reminders</code> - Reminder times in minutes before tasting (e.g., <code>30,10</code>)
          • <code>lockoutBefore</code> - Minutes before tasting when registration locks. Set to <code>0</code> to turn off.
          • <code>botActive</code> - Enable/disable bot (<code>true</code>/<code>false</code>)
          • <code>timeZone</code> - Time zone for scheduling (e.g., <code>UTC</code>, <code>Europe/Berlin</code>)   

        <b>User Commands:</b>
        /join - Join today's tea tasting session
        /leave - Leave today's tea tasting session
        """.trimIndent()
            )
        }

//        onCommand("cancelToday") { message ->
//            if (!message.inGroupChat(session.targetChatID)) return@onCommand
//        }

        // User commands

        onCommand("start") { message ->
            replyHtml(
                message,
                """
        👋 Welcome to Thoughts & Tea Bot!

        This bot helps organize regular tea tasting sessions in your group. Here's how it works:

        1️⃣ On scheduled <days></days>, the bot will ask who wants to join the tea tasting
        2️⃣ Use /join to register for today's session
        3️⃣ Use /leave to unregister if your plans change
        4️⃣ The bot will notify everyone when it's time for tea!

        <b>Available Commands:</b>
        /join - Join today's tea tasting session
        /leave - Leave today's tea tasting session

        Enjoy your tea! 🍵
        """.trimIndent()
            )
        }

        onCommand("join") { message ->
            if (!message.inGroupChat(session.targetChatID)) return@onCommand
            val from = message.fromUserOrNull()?.from ?: return@onCommand
            val fromId = from.id

            if (session.tastingState.value != TastingState.ANNOUNCED) {
                reply(message, "⚠️ Sorry, new participants are not being registered right now.")
                return@onCommand
            }

            if (session.tastingState.value == TastingState.DEFAULT) return@onCommand
            if (!session.addParticipant(fromId, from))
                reply(message, reply(message, "✋ You're already registered for today's tea tasting!"))
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
