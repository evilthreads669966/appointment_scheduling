import org.jetbrains.exposed.dao.id.IntIdTable

object Persons: IntIdTable(){
    val name = varchar("name", 25).uniqueIndex()
    val age = integer("age")
}