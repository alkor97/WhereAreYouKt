package info.alkor.whereareyou.model.action

data class PhoneNumber private constructor(val value: String) {
    fun toHumanReadable() = "+" + value.removePrefix("+")
            .reversed()
            .chunked(3)
            .joinToString(" ")
            .reversed()

    fun toExternalForm() = value.replaceFirst("+", "00")

    fun isValid() = !value.isEmpty()

    companion object {
        operator fun invoke(value: String): PhoneNumber {
            val normalized = value.trim()
                    .replace("\\s+".toRegex(), "")
                    .replace("^\\+".toRegex(), "00")
                    .replace("\\D".toRegex(), "")
                    .replace("^00+".toRegex(), "+")
            return PhoneNumber(normalized)
        }

        private operator fun invoke() = PhoneNumber("")
        val OWN = PhoneNumber()
    }
}