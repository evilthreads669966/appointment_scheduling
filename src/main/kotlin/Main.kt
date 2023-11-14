/*
Copyright 2023 Chris Basinger

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

import Appointments.entityId
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.concurrent.timer

val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

fun main(args: Array<String>) {
    val db = Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

    transaction {
        SchemaUtils.create(Persons, Appointments)
    }

    val delay = 60 * 15 * 1000L
    val timer = timer("missed", true, delay, delay){
        val datetime = LocalDateTime.now()
        transaction {
            val appointments = AppointmentEntity.find { Appointments.checkedin eq false and Appointments.missed.eq(false) }
            appointments.forEach{
                val otherDateTime = LocalDateTime.parse(it.datetime, formatter)
                if(otherDateTime.compareTo(datetime) == -1)
                    it.missed = true
            }
        }

    }
    val banner = " _____      _ _   _____      _              _       _ _             \n" +
            "|  ___|    (_) | /  ___|    | |            | |     | (_)            \n" +
            "| |____   ___| | \\ `--.  ___| |__   ___  __| |_   _| |_ _ __   __ _ \n" +
            "|  __\\ \\ / / | |  `--. \\/ __| '_ \\ / _ \\/ _` | | | | | | '_ \\ / _` |\n" +
            "| |___\\ V /| | | /\\__/ / (__| | | |  __/ (_| | |_| | | | | | | (_| |\n" +
            "\\____/ \\_/ |_|_| \\____/ \\___|_| |_|\\___|\\__,_|\\__,_|_|_|_| |_|\\__, |\n" +
            "                                                               __/ |\n" +
            "                                                              |___/ "
    println(banner)

    while(true){
        menu()
        while(true){
            val input = readlnOrNull()?.trim()?.lowercase()
            if(input.isNullOrBlank()) continue
            when(input){
                "persons" -> persons()
                "add person" -> addPerson()
                "add appointment" -> addAppointment()
                "appointments" -> appointments()
                "delete person" -> deletePerson()
                "delete appointment" -> deleteAppointment()
                "check in" -> checkin()
                "past appointments" -> pastAppointments()
                "missed appointments" -> missedAppointments()
                "exit" -> {
                    timer.cancel()
                    System.exit(0)
                }
            }
            break
        }
    }
}

fun pastAppointments() {
    val appointments = transaction {
        AppointmentEntity.find { Appointments.checkedin eq true }.map { it.toAppointment() }
    }

    appointments.forEach { println(it) }
}

fun missedAppointments() {
    val appointments = transaction {
        AppointmentEntity.find { Appointments.missed eq true }.map { it.toAppointment() }
    }

    appointments.forEach { println(it) }
}

fun checkin(){
    while(true){
        println("Enter the appointment ID that you are checking in for")
        print("ID: ")
        val input = readlnOrNull()
        if(input.isNullOrBlank()) continue
        if(input == "cancel") break
        val id = input.toIntOrNull()
        if(id == null) continue
        transaction {
            AppointmentEntity.findById(id)?.checkedin = true
        }
        break
    }
}

fun deleteAppointment() {
    while(true){
        println("Enter the id of the appointment to delete")
        print("ID: ")
        val input = readlnOrNull()
        if(input.isNullOrBlank()) continue
        if(input.lowercase() == "cancel") break
        val id = input.toIntOrNull()
        if(id == null) continue
        transaction{
            AppointmentEntity.findById(id)?.delete()
        }
        break
    }
}

fun deletePerson() {
    while(true){
        print("Enter ID of person to delete")
        print("\nID: ")
        val input = readlnOrNull()
        if(input.isNullOrBlank()) continue
        if(input.lowercase() == "cancel") break
        val id = input.toIntOrNull()
        if(id == null) continue
        transaction {
            val appointments =  AppointmentEntity.find{ Appointments.person.entityId() eq EntityID(id, Persons) }
            appointments.forEach { it.delete() }
            PersonEntity.findById(id)?.delete()
        }
        println("Person and appointments deleted")
        break
    }

}

fun menu(){
    println()
    println("Enter one of the following options:")
    println("Persons")
    println("Appointments")
    println("Add Person")
    println("Add Appointment")
    println("Delete Person")
    println("Delete Appointment")
    println("Check In")
    println("Missed Appointments")
    println("Exit")
    println()
}

fun appointments(){
    val appointments = transaction {
        AppointmentEntity.find( Appointments.checkedin eq  false and  Appointments.missed.eq(false)).map { it.toAppointment() }
    }
    if(appointments.isEmpty())
        println("No appointments have been scheduled yet")
    else
        appointments.toMutableList().sortedBy { it.datetime }.forEach {
            println(it)
            println()
        }
}

fun addAppointment(){

    while(true){
        print("Enter the ID of the person: ")
        var input = readlnOrNull()
        if(input.isNullOrBlank()) continue
        if(input.lowercase() == "cancel") break
        val id = input.toIntOrNull()
        if(id == null) continue
        println("Enter the time and date of the appointment in this format: yyyy-MM-dd HH:mm")
        input = readlnOrNull()?.trim()
        if(input.isNullOrBlank()) continue
        if(input.lowercase() == "cancel") break
        val date = input
        transaction {
            AppointmentEntity.new {
                datetime = date
                this.person = PersonEntity[id]
            }
        }
        break
    }
}



fun persons(){
    val persons = transaction {
        PersonEntity.all().map { it.toPerson() }
    }
    if(persons.isEmpty())
        println("No people have been added yet")
    else
        persons.forEach {
            println(it)
            println()
        }
}

fun addPerson(){
    loop@while(true){
        var name: String? = null
        var input: String? = null
        name@while(true){
            print("New Person Name: ")
            input = readlnOrNull()?.trim()
            if(input.isNullOrBlank()) continue@name
            if(input.lowercase() == "cancel") break@loop
            name = input
            break@name
        }
        age@while(true){
            print("New Person Age: ")
            input = readlnOrNull()?.trim()
            if(input.isNullOrBlank()) continue@age
            if(input.lowercase() == "cancel") break@loop
            val age = input.toIntOrNull()
            if(age == null) continue@age
            break@age
        }
        transaction {
            PersonEntity.new {
                this.name = name!!
                this.age = age!!
            }
        }
        break@loop
    }
}
