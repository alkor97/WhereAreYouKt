package info.alkor.whereareyou.model.action

import java.lang.IllegalArgumentException

data class PhoneNumber private constructor(val value: String) {
    fun toHumanReadable() = "+" + value.removePrefix("+")
            .reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()

    fun toExternalForm() = value.replaceFirst("+", "00")

    companion object {
        operator fun invoke(value: String): PhoneNumber {
            val normalized = value.trim()
                    .replace("\\s+".toRegex(), "")
                    .replace("^\\+".toRegex(), "00")
                    .replace("^00+".toRegex(), "+")
            if (!normalized.matches(normalizedForm))
                throw IllegalArgumentException(value)
            return PhoneNumber(normalized)
        }

        private val normalizedForm = "^[\\+^0]\\d+$".toRegex()
    }
}