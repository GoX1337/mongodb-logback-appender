package org.gox.logback.appender;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import com.mongodb.client.*;
import org.bson.Document;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class MongoDbAppenderTest {

    private static final Logger log = LoggerFactory.getLogger(MongoDbAppenderTest.class);
    private static MongoDBContainer mongoContainer;
    private static MongoClient mongoClient;
    private static String replicaSetUrl;

    @BeforeAll
    static void setUp() {
        mongoContainer = new MongoDBContainer("mongo:6.0");
        mongoContainer.start();
        replicaSetUrl = mongoContainer.getReplicaSetUrl();
        mongoClient = MongoClients.create(replicaSetUrl);
    }

    @AfterAll
    static void tearDown() {
        mongoClient.close();
        mongoContainer.stop();
    }

    @Test
    void testMongoDbAppender() throws InterruptedException {
        MongoDbAppender mongoDbAppender = new MongoDbAppender();
        mongoDbAppender.setConnectionString(replicaSetUrl);
        System.out.println(replicaSetUrl);
        mongoDbAppender.setCollectionName("testLogs");
        mongoDbAppender.start();

        LoggingEvent loggingEvent = new LoggingEvent();
        loggingEvent.setInstant(Instant.now());
        loggingEvent.setThreadName("Test");
        loggingEvent.setLevel(Level.INFO);
        loggingEvent.setLoggerName("LOGGER");
        loggingEvent.setMessage("HELLO THERE");

        mongoDbAppender.append(loggingEvent);
        Thread.sleep(1000);

        MongoCollection<Document> logs = mongoClient.getDatabase("test").getCollection("testLogs");
        assertThat(logs.countDocuments()).isEqualTo(1);

        assertThat(Optional.ofNullable(logs.find().first())
                .map(document -> document.getString("message"))
                .stream()
                .findFirst()
                .orElseThrow()
        ).isEqualTo("HELLO THERE");
    }
}