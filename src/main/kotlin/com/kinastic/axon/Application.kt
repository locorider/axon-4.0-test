package com.kinastic.axon

import com.kinastic.axon.model.CreateTodo
import org.axonframework.commandhandling.gateway.CommandGateway
import org.axonframework.common.IdentifierFactory
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.lang.Exception
import java.util.*

@SpringBootApplication
@EnableScheduling
open class Application {

    companion object {
        val log = LoggerFactory.getLogger(Application::class.java)
    }

    @Autowired
    lateinit var commandGateway: CommandGateway

    @Scheduled(fixedRate = 2000)
    fun emit() {
        try {
            commandGateway.send<Any>(CreateTodo(IdentifierFactory.getInstance().generateIdentifier(), UUID.randomUUID().toString()))
        } catch (e: Exception) {
            log.error(e.message, e)
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}