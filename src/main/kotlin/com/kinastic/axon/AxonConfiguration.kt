package com.kinastic.axon

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.MongoClient
import org.axonframework.axonserver.connector.AxonServerConfiguration
import org.axonframework.axonserver.connector.AxonServerConnectionManager
import org.axonframework.axonserver.connector.event.axon.AxonServerEventStore
import org.axonframework.config.Configurer
import org.axonframework.eventhandling.EventBus
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration
import org.axonframework.eventhandling.async.SequentialPerAggregatePolicy
import org.axonframework.eventhandling.async.SequentialPolicy
import org.axonframework.eventhandling.scheduling.java.SimpleEventScheduler
import org.axonframework.eventhandling.tokenstore.TokenStore
import org.axonframework.eventsourcing.eventstore.EventStore
import org.axonframework.extensions.mongo.DefaultMongoTemplate
import org.axonframework.extensions.mongo.eventhandling.saga.repository.MongoSagaStore
import org.axonframework.extensions.mongo.eventsourcing.tokenstore.MongoTokenStore
import org.axonframework.modelling.saga.repository.SagaStore
import org.axonframework.serialization.Serializer
import org.axonframework.serialization.json.JacksonSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.Executors

@Configuration
open class AxonConfiguration {

    @Autowired
    lateinit var mongoClient: MongoClient

    @Qualifier("eventSerializer")
    @Bean
    open fun eventSerializer(): Serializer {
        return JacksonSerializer.builder().objectMapper(ObjectMapper()
                .registerModule(KotlinModule())
                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        ).build()
    }

    @Bean
    open fun eventScheduler(eventBus: EventBus) =
            SimpleEventScheduler.builder()
                    .scheduledExecutorService(Executors.newScheduledThreadPool(4))
                    .eventBus(eventBus).build()

    @Autowired
    fun configure(configurer: Configurer) {
        configurer
                .eventProcessing { epc ->
                    epc.registerDefaultSequencingPolicy { SequentialPerAggregatePolicy() }
                }
                .registerComponent(TrackingEventProcessorConfiguration::class.java) { c ->
                    TrackingEventProcessorConfiguration
                            .forParallelProcessing(4)
                            .andInitialSegmentsCount(4)
                }
    }

    @Bean
    open fun eventSourcingTokenStore(serializer: Serializer): TokenStore {
        return MongoTokenStore.builder()
                .mongoTemplate(axonMongoTemplate())
                .serializer(serializer)
                .build()
    }

    @Bean
    open fun axonMongoTemplate(): DefaultMongoTemplate {
        return DefaultMongoTemplate.builder()
                .mongoDatabase(mongoClient, "axon_test")
                .build()
    }

    @Bean
    open fun eventStore(axonServerConfiguration: AxonServerConfiguration, axonServerConnectionManager: AxonServerConnectionManager): EventStore {
        axonServerConfiguration.nrOfNewPermits = 1000
        axonServerConfiguration.initialNrOfPermits = 2000

        return AxonServerEventStore.builder()
                .configuration(axonServerConfiguration)
                .eventSerializer(eventSerializer())
                .platformConnectionManager(axonServerConnectionManager)
                .build()
    }

    @Bean
    open fun sagaRepository(serializer: Serializer): SagaStore<Any> {
        return MongoSagaStore.builder()
                .mongoTemplate(axonMongoTemplate())
                .serializer(serializer)
                .build()
    }
}