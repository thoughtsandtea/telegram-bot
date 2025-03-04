package club.thoughtsandtea.thoughtsntea.utils

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.api.send.sendTextMessage
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.*
import dev.inmo.tgbotapi.types.business_connection.BusinessConnectionId
import dev.inmo.tgbotapi.types.buttons.KeyboardMarkup
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.HTML
import dev.inmo.tgbotapi.types.message.abstracts.AccessibleMessage
import dev.inmo.tgbotapi.types.message.abstracts.ContentMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.starry.ktscheduler.job.Job
import dev.starry.ktscheduler.scheduler.KtScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DayOfWeek
import org.intellij.lang.annotations.Language
import java.time.LocalTime
import java.util.*

internal fun getenvOrFail(key: String): String = checkNotNull(System.getenv(key)) {
    "Environment variable $key is not set"
}

internal suspend fun TextMessage.isFromAdministratorUser(api: TelegramBot, chatId: ChatId): Boolean {
    val uid = (fromUserOrNull()?.user ?: return false).id
    val adminsIds = api.getChatAdministrators(chatId).map { it.user.id }
    return uid in adminsIds
}

internal fun TextMessage.inGroupChat(withId: ChatId? = null) = chat is GroupChat && (withId == null || chat.id == withId)

internal fun KtScheduler.runWeekly(
    jobId: String = "runDaily-${UUID.randomUUID()}",
    dailyTime: LocalTime,
    daysOfWeek: Set<DayOfWeek>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    runConcurrently: Boolean = true,
    block: suspend () -> Unit
): String {
    addJob(
        Job(
            jobId = jobId,
            trigger = WeeklyTrigger(dailyTime, daysOfWeek),
            runConcurrently = runConcurrently,
            dispatcher = dispatcher,
            callback = block,
        )
    )
    return jobId
}

internal inline fun <reified T : Enum<T>> emptyEnumSet(): EnumSet<T> = EnumSet.noneOf(T::class.java)

internal inline fun <reified T : Enum<T>> enumSetOf(vararg elements: T): EnumSet<T> {
    return when (elements.size) {
        0 -> emptyEnumSet<T>()
        1 -> EnumSet.of(elements[0])
        else -> EnumSet.of(elements[0], *elements.sliceArray(1..elements.lastIndex))
    }
}

internal suspend fun TelegramBot.sendHtml(
    chatId: ChatIdentifier,
    @Language("HTML")
    text: String,
    linkPreviewOptions: LinkPreviewOptions? = null,
    threadId: MessageThreadId? = chatId.threadId,
    businessConnectionId: BusinessConnectionId? = chatId.businessConnectionId,
    disableNotification: Boolean = false,
    protectContent: Boolean = false,
    allowPaidBroadcast: Boolean = false,
    effectId: EffectId? = null,
    replyParameters: ReplyParameters? = null,
    replyMarkup: KeyboardMarkup? = null
): ContentMessage<TextContent> = sendTextMessage(
    chatId = chatId,
    text = text,
    parseMode = HTML,
    linkPreviewOptions = linkPreviewOptions,
    threadId = threadId,
    businessConnectionId = businessConnectionId,
    disableNotification = disableNotification,
    protectContent = protectContent,
    allowPaidBroadcast = allowPaidBroadcast,
    effectId = effectId,
    replyParameters = replyParameters,
    replyMarkup = replyMarkup
)

internal suspend inline fun TelegramBot.replyHtml(
    to: AccessibleMessage,
    @Language("HTML")
    text: String,
    linkPreviewOptions: LinkPreviewOptions? = null,
    replyInChatId: IdChatIdentifier = to.chat.id,
    replyInThreadId: MessageThreadId? = replyInChatId.threadId,
    replyInBusinessConnectionId: BusinessConnectionId? = replyInChatId.businessConnectionId,
    disableNotification: Boolean = false,
    protectContent: Boolean = false,
    allowPaidBroadcast: Boolean = false,
    effectId: EffectId? = null,
    allowSendingWithoutReply: Boolean? = null,
    replyMarkup: KeyboardMarkup? = null
): ContentMessage<TextContent> = sendTextMessage(
    chatId = replyInChatId,
    text = text,
    parseMode = HTML,
    linkPreviewOptions = linkPreviewOptions,
    threadId = replyInThreadId,
    businessConnectionId = replyInBusinessConnectionId,
    disableNotification = disableNotification,
    protectContent = protectContent,
    allowPaidBroadcast = allowPaidBroadcast,
    effectId = effectId,
    replyParameters = ReplyParameters(to.metaInfo, allowSendingWithoutReply = allowSendingWithoutReply),
    replyMarkup = replyMarkup
)
