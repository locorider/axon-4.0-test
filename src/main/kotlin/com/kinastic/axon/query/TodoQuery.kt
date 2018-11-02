package com.kinastic.axon.query

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "todos")
class TodoQuery(@get:Id val id: String,
                val name: String) {
}