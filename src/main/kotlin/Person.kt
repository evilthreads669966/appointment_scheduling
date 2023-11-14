data class Person(val id: Int, val name: String, val age: Int){
    override fun toString(): String {
        return "ID: $id\nName: $name\nAge: $age"
    }
}