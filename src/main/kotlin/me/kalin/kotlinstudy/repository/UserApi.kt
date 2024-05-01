package me.kalin.kotlinstudy.repository

import kotlinx.coroutines.flow.Flow
import me.kalin.kotlinstudy.dto.UserResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.reactive.function.client.bodyToFlow

@Service
class UserApi(
    private val webClient: WebClient
) {
    fun findAllUsersFlow(): Flow<UserResponse> =
        webClient.get()
            .uri("/users")
            .retrieve()
            .bodyToFlow()

    suspend fun findAllUsersUsingExchange(): List<UserResponse> =
        webClient.get()
            .uri("/users")
            .awaitExchange { clientResponse ->
                val headers = clientResponse.headers().asHttpHeaders()
                clientResponse.awaitBody()
            }
}