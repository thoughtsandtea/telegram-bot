package dev.teaguild.thoughtsntea

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.IOException
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.jvm.Throws
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty

private val configFile = Path("thoughtsntea.json")

@Serializable
private data class TeaTastingConfig(
    var daysOfWeek: List<String> = listOf("Tuesday", "Thursday"),  // Stored as String for serialization
    var askTime: String = "07:00",  // Stored as String for serialization
    var tastingTime: String = "16:00",  // Stored as String for serialization
    var maxParticipants: Int = 5,
    var reminders: List<Int> = listOf(30, 10),
    var lockoutBefore: Int = 10,
    var botActive: Boolean = true
)

private var config = TeaTastingConfig()

@Throws(IOException::class)
private fun saveDefaultConfig() {
    config = TeaTastingConfig()
    saveConfig()
}

@Throws(IOException::class)
private fun saveConfig() {
    configFile.writeText(Json.encodeToString(config))
}

@Throws(IOException::class)
fun loadConfig() {
    if (!configFile.exists())
        saveDefaultConfig()
    else
        try {
            config = Json.decodeFromString(configFile.readText())
        } catch (e: SerializationException) {
            saveDefaultConfig()
        }
}

private fun <T> configProperty(configProperty: KMutableProperty1<TeaTastingConfig, T>): ReadWriteProperty<Any?, T> =
    object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T = configProperty.get(config)

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            configProperty.set(config, value)
            saveConfig()
        }
    }

private fun <C, T, R> ReadWriteProperty<C, T>.map(
    transformTo: (T) -> R,
    transformFrom: (R) -> T,
): ReadWriteProperty<C, R> = object : ReadWriteProperty<C, R> {
    override fun getValue(thisRef: C, property: KProperty<*>): R = transformTo(this@map.getValue(thisRef, property))

    override fun setValue(thisRef: C, property: KProperty<*>, value: R) =
        this@map.setValue(thisRef, property, transformFrom(value))
}

var daysOfWeek by configProperty(TeaTastingConfig::daysOfWeek).map(
    { value -> value.map { DayOfWeek.valueOf(it.uppercase()) } },
    { value -> value.map { it.name } }
)


var askTimeLocal: LocalTime by configProperty(TeaTastingConfig::askTime).map(
    { LocalTime.parse(it) },
    { it.toString() },
)

var tastingTimeLocal: LocalTime by configProperty(TeaTastingConfig::tastingTime).map(
    { LocalTime.parse(it) },
    { it.toString() },
)

var maxParticipants by configProperty(TeaTastingConfig::maxParticipants)
var lockoutBefore by configProperty(TeaTastingConfig::lockoutBefore)
var botActive by configProperty(TeaTastingConfig::botActive)
