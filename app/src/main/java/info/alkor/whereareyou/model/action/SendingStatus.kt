package info.alkor.whereareyou.model.action

enum class SendingStatus {
    PENDING,
    SENT,
    SENDING_FAILED,
    DELIVERED,
    DELIVERY_FAILED
}

fun SendingStatus.finishesSending() = when (this) {
    SendingStatus.SENDING_FAILED -> true
    SendingStatus.DELIVERY_FAILED -> true
    SendingStatus.DELIVERED -> true
    else -> false
}