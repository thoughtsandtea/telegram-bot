package dev.teaguild.thoughtsntea

import dev.inmo.kslog.common.logger
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.starry.ktscheduler.job.Job

suspend fun main() {
    val token = checkNotNull(System.getenv("THOUGHTSNTEA_TELEGRAM_BOT_TOKEN")) {
        "No environment variable THOUGHTSNTEA_TELEGRAM_BOT_TOKEN"
    }
    Job

    loadConfig()

    val bot = telegramBot(token)

    bot.buildBehaviourWithLongPolling {
        println(getMe())
    }.join()
}
