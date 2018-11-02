package com.kinastic.axon.query

import com.kinastic.axon.model.TodoCreated
import org.axonframework.config.ProcessingGroup
import org.axonframework.eventhandling.EventHandler
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
@ProcessingGroup("TodoEventHandler")
open class TodoEventHandler(private val mongoTemplate: MongoTemplate) {

    @EventHandler
    fun on (event: TodoCreated) {
        mongoTemplate.save(TodoQuery(event.todoId, event.name))
    }
}
