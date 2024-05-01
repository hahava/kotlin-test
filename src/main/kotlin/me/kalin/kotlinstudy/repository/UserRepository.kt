package me.kalin.kotlinstudy.repository

import kotlinx.coroutines.suspendCancellableCoroutine
import me.kalin.kotlinstudy.entity.User
import org.slf4j.LoggerFactory
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import javax.xml.crypto.Data
import kotlin.coroutines.resume


interface UserRepositoryInterface : CoroutineCrudRepository<User, Long> {
    override suspend fun findById(id: Long): User?

    @Modifying
    @org.springframework.data.r2dbc.repository.Query("UPDATE user SET id = :id")
    suspend fun updateName(id: String): Int
}

@Repository
class UserRepositoryTemplate(
    private val template: R2dbcEntityTemplate
) {
    suspend fun findById(id: Long): User? =
        template.select(User::class.java)
            .matching(
                Query.query(
                    Criteria.where("id").`is`(id)
                )
            )
            .awaitOneOrNull()
}

@Repository
class UserRepositoryClient(
    private val client: DatabaseClient,
) {
    suspend fun findById(id: Long): User? =
        client
            .sql("SELECT * FROM user WHERE id = $id")
            .map { row ->
                User(
                    row.get("id") as Long,
                    row.get("username") as String,
                    row.get("email") as String,
                )
            }
            .awaitOneOrNull()
}

/*
* callback...
* */
@Repository
class UserDao(
    private val jdbcTemplate: JdbcTemplate
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun findById(id: Long): User? = jdbcTemplate.queryForObject("select * from user where id =?")
    { rs, _ ->
        User(
            id = rs.getString("id").toLong(),
            username = rs.getString("username"),
            email = rs.getString("email")
        )
    }

    suspend fun getUserById(id: Long): User? = suspendCancellableCoroutine { continuation ->
        val sql = "SELECT * FROM users WHERE id = ?"

        try {
            jdbcTemplate.query(sql, arrayOf(id)) { rs ->
                if (rs.next()) {
                    val user = User(
                        id = rs.getString("id").toLong(),
                        username = rs.getString("username"),
                        email = rs.getString("email")
                    )

                    continuation.resume(user)
                } else {
                    continuation.resume(null)
                }
            }
        } catch (e: Exception) {
            continuation.invokeOnCancellation {
                logger.error("failed...", it)
            }
        }
    }
}
