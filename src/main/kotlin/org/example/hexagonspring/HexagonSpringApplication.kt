package org.example.hexagonspring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableAsync
class HexagonSpringApplication

fun main(args: Array<String>) {
    runApplication<HexagonSpringApplication>(*args)
}
