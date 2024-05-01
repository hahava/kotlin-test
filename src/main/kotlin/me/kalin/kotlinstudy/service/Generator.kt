package me.kalin.kotlinstudy.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class Generator {
    fun generateUUIDMono(): Mono<String> = Mono.create {
            UUID.randomUUID().toString()
        }

    suspend fun generateUUID(): String = withContext(Dispatchers.Default) {
        UUID.randomUUID().toString()
    }
}