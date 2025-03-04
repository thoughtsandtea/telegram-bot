package club.thoughtsandtea.thoughtsntea.utils

import dev.starry.ktscheduler.triggers.Trigger
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class WeeklyTrigger(private val time: LocalTime, private val daysOfWeek: Set<DayOfWeek>) : Trigger {
    override fun getNextRunTime(currentTime: ZonedDateTime, timeZone: ZoneId): ZonedDateTime {
        // Convert current time to the specified time zone
        var nextRunTime = currentTime
            .withZoneSameInstant(timeZone)
            .with(time) // set hour/minute/second from [time]
            .withNano(0) // clear out any leftover nanoseconds

        // If the day is not in the allowed set or the time is now/past, roll forward
        if (nextRunTime.dayOfWeek !in daysOfWeek || !nextRunTime.isAfter(currentTime)) {
            do {
                nextRunTime = nextRunTime.plusDays(1)
            } while (nextRunTime.dayOfWeek !in daysOfWeek)
        }

        return nextRunTime
    }
}
