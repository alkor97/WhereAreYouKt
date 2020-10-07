package info.alkor.whereareyou.impl.persistence

import info.alkor.whereareyou.model.action.Person
import info.alkor.whereareyou.model.action.PhoneNumber

fun Person.toRecord() = PersonRecord(
        phone = phone.value,
        name = name
)

fun PersonRecord.toModel() = Person(
        phone = PhoneNumber(phone),
        name = name
)
