import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.time.LocalDateTime

class AppointmentEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AppointmentEntity>(Appointments)
    var datetime by Appointments.datetime
    var checkedin by Appointments.checkedin
    var missed by Appointments.missed
    var person by PersonEntity referencedOn Appointments.person

    fun toAppointment() = Appointment(id.value, LocalDateTime.parse(datetime, formatter), checkedin, missed, person.toPerson())
}