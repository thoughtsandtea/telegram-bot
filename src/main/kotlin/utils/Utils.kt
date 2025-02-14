package dev.teaguild.thoughtsntea.utils

import dev.inmo.tgbotapi.bot.TelegramBot
import dev.inmo.tgbotapi.extensions.api.chat.get.getChatAdministrators
import dev.inmo.tgbotapi.extensions.utils.fromUserOrNull
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.chat.GroupChat
import dev.inmo.tgbotapi.types.message.content.TextMessage
import dev.starry.ktscheduler.job.Job
import dev.starry.ktscheduler.scheduler.KtScheduler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.DayOfWeek
import java.time.LocalTime
import java.util.EnumSet
import java.util.UUID

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
