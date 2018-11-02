package com.kinastic.axon.model

import org.axonframework.commandhandling.CommandHandler
import org.axonframework.eventsourcing.EventSourcingHandler
import org.axonframework.modelling.command.AggregateIdentifier
import org.axonframework.modelling.command.AggregateLifecycle
import org.axonframework.modelling.command.TargetAggregateIdentifier
import org.axonframework.spring.stereotype.Aggregate

@Aggregate(type = "Todo")
open class TodoAggregate {

    @AggregateIdentifier
    lateinit var todoId: String
    lateinit var name: String

    constructor() {

    }

    @CommandHandler
    constructor(command: CreateTodo) {
        AggregateLifecycle.apply(TodoCreated(command.todoId, command.name))
    }

    @EventSourcingHandler
    fun on (event: TodoCreated) {
        todoId = event.todoId
        name = event.name
    }
}