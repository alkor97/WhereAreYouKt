package info.alkor.whereareyou.model.communication

import info.alkor.whereareyou.model.action.Person

data class Message(val from: Person, val body: String)