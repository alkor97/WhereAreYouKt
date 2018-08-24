package info.alkor.whereareyou.common

data class AccurateValue<T>(val value: T, val accuracy: T? = null) {
    override fun toString() = "$value" + if (accuracy != null) " ±$accuracy" else ""
}
