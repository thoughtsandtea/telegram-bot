package dev.teaguild.thoughtsntea

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.content.TextMessage

fun getenvOrFail(key: String): String = checkNotNull(System.getenv(key)) {
    "Environment variable $key is not set"
}

suspend fun TextMessage.isFromAdministratorUser(api: TelegramBot, chatId: ChatId): Boolean {
    val uid = (fromUserOrNull()?.user ?: return false).id
    val adminsIds = api.getChatAdministrators(chatId).map { it.user.id }
    return uid in adminsIds
}

fun TextMessage.inGroupChat(withId: ChatId? = null) = chat is GroupChat && (withId == null || chat.id == withId)
