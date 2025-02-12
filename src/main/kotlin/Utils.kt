package dev.teaguild.thoughtsntea

fun getenvOrFail(key: String): String = checkNotNull(System.getenv(key)) {
    "Environment variable $key is not set"
}
