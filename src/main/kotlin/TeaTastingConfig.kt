package dev.teaguild.thoughtsntea

import dev.teaguild.thoughtsntea.utils.emptyEnumSet
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.Blocking
import java.io.IOException
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

private val configFile = Path("thoughtsntea.json")


@Serializable
private data class TeaTastingConfigSurrogate(
    val daysOfWeek: List<String>,
    val askTimeString: String,
    val tastingTimeString: String,
    val maxParticipants: Int,
    val remindersMinutes: List<Long>,
    val lockoutBeforeMinutes: Long,
    val botActive: Boolean,
    val timeZone: String,
)


@Serializable(with = TeaTastingConfigSerializer::class)
data class TeaTastingConfig(
    val daysOfWeek: Set<DayOfWeek> = setOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
    val askTime: LocalTime = LocalTime.parse("07:00"),
    val tastingTime: LocalTime = LocalTime.parse("16:00"),
    val maxParticipants: Int = 5,
    val remindersMinutes: List<Long> = listOf(30, 10),
    val lockoutBeforeMinutes: Long = 10,
    val botActive: Boolean = true,
    val timeZone: ZoneId = ZoneId.of("UTC")
) {
    val reminders get() = remindersMinutes.map { Duration.ofMinutes(it) }
    val lockoutBefore get() = Duration.ofMinutes(lockoutBeforeMinutes)

    companion object {
        val Default = TeaTastingConfig()
    }
}

private object TeaTastingConfigSerializer : KSerializer<TeaTastingConfig> {
    // Serial names of descriptors should be unique, so we cannot use ColorSurrogate.serializer().descriptor directly
    override val descriptor: SerialDescriptor = SerialDescriptor(TeaTastingConfig::class.qualifiedName!!, TeaTastingConfigSurrogate.serializer().descriptor)

    override fun serialize(encoder: Encoder, value: TeaTastingConfig) {
        val surrogate = TeaTastingConfigSurrogate(
            daysOfWeek = value.daysOfWeek.map { it.name },
            askTimeString = value.askTime.toString(),
            tastingTimeString = value.tastingTime.toString(),
            maxParticipants = value.maxParticipants,
            remindersMinutes = value.remindersMinutes,
            lockoutBeforeMinutes = value.lockoutBeforeMinutes,
            botActive = value.botActive,
            timeZone = value.timeZone.id
        )
        encoder.encodeSerializableValue(TeaTastingConfigSurrogate.serializer(), surrogate)
    }

    override fun deserialize(decoder: Decoder): TeaTastingConfig {
        val surrogate = decoder.decodeSerializableValue(TeaTastingConfigSurrogate.serializer())
        return TeaTastingConfig(
            daysOfWeek = surrogate.daysOfWeek.mapTo(emptyEnumSet()) { DayOfWeek.valueOf(it) },
            askTime = LocalTime.parse(surrogate.askTimeString),
            tastingTime = LocalTime.parse(surrogate.tastingTimeString),
            maxParticipants = surrogate.maxParticipants,
            remindersMinutes = surrogate.remindersMinutes,
            lockoutBeforeMinutes = surrogate.lockoutBeforeMinutes,
            botActive = surrogate.botActive,
            timeZone = ZoneId.of(surrogate.timeZone),
        )
    }
}

private val format = Json { prettyPrint = true }

@Blocking
@Throws(IOException::class)
fun saveConfig(config: TeaTastingConfig) {
    configFile.writeText(format.encodeToString(config))
}

@Blocking
@Throws(IOException::class)
fun loadConfig(default: TeaTastingConfig = TeaTastingConfig.Default): TeaTastingConfig = if (!configFile.exists()) {
    saveConfig(default)
    default
} else {
    try {
        format.decodeFromString(configFile.readText())
    } catch (_: SerializationException) {
        saveConfig(default)
        default
    }
}

