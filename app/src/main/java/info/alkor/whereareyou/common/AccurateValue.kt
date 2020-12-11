package info.alkor.whereareyou.common

interface WithDouble {
    val value: Double
}

data class AccurateValue<T : WithDouble>(val value: T, val accuracy: T? = null) {
    override fun toString() = "$value" + if (accuracy != null) " ±$accuracy" else ""
}
