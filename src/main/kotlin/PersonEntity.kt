import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class PersonEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<PersonEntity>(Persons)
    var name by Persons.name
    var age by Persons.age


    fun toPerson() = Person(id.value, name,age)
}