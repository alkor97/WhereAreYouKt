package info.alkor.whereareyou.model.action

data class MessageId(val id: Long) : Comparable<MessageId> {
    override fun compareTo(other: MessageId): Int = (id - other.id).toInt()
}