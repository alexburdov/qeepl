package ru.alex.burdovitsin.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import ru.alex.burdovitsin.model.User

@Repository
interface UserRepository : MongoRepository<User, String> {
    fun findByUsername(username: String): User?
}