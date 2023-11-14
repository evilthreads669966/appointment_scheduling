import java.time.LocalDateTime

data class Appointment(val id: Int, val datetime: LocalDateTime, val checkedin: Boolean, val missed: Boolean, val person: Person){
    override fun toString(): String {
        return "appointment ID: $id\nperson ID: ${person.id}\nname: ${person.name}\ndate: ${datetime.format(formatter)}"
    }
}