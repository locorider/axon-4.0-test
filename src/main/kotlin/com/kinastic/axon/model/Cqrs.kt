package com.kinastic.axon.model

import org.axonframework.modelling.command.TargetAggregateIdentifier

data class CreateTodo(@field:TargetAggregateIdentifier val todoId: String,
                      val name: String)

data class TodoCreated(val todoId: String, val name: String)