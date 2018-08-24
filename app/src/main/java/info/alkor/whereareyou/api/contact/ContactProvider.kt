package info.alkor.whereareyou.api.contact

import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber

interface ContactProvider {
    fun findName(phone: PhoneNumber): Person
}