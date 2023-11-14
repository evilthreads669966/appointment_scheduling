import org.jetbrains.exposed.dao.id.IntIdTable

object Appointments: IntIdTable(){
    val datetime = varchar("date", 18)
    val person = reference("person", Persons)
    val checkedin = bool("checkedin").default(false)
    val missed = bool("missed").default(false)
}