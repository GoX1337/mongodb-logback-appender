package org.gox.logback.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;

public class MongoDbAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final Logger logger = LoggerFactory.getLogger(MongoDbAppender.class);

    private String connectionString;
    private String collectionName;
    private MongoClient mongoClient;

    @Override
    public void start() {
        try {
            MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(connectionString))
                    .build();
            this.mongoClient = MongoClients.create(mongoClientSettings);
            super.start();
            logger.info("MongoDB appender started: {}", isStarted());
        } catch (Exception e) {
            logger.error("Error creating MongoDB appender", e);
        }
    }

    @Override
    public void stop() {
        this.mongoClient.close();
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent event) {
        MongoCollection<Document> collection = mongoClient
                .getDatabase("test")
                .getCollection(this.collectionName);

        Document document = new Document();
        document.append("timestamp", event.getInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                .append("level", event.getLevel().toString())
                .append("logger", event.getLoggerName())
                .append("message", event.getFormattedMessage())
                .append("thread", event.getThreadName());

        Thread.startVirtualThread(() -> {
            InsertOneResult insertOneResult = collection.insertOne(document);
            if (logger.isDebugEnabled()) {
                logger.debug("Inserted document : {}, ACK : {}", insertOneResult, insertOneResult.wasAcknowledged());
            }
        });
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}