import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LambdaCSVProcessor implements RequestHandler<S3Event, String> {

    private final AmazonS3 s3Client;
    private final AmazonDynamoDB dynamoDbClient;

    public LambdaCSVProcessor() {
        Injector injector = Guice.createInjector(new AwsServicesModule());
        this.s3Client = injector.getInstance(AmazonS3.class);
        this.dynamoDbClient = injector.getInstance(AmazonDynamoDB.class);
    }

    @Override
    public String handleRequest(S3Event event, Context context) {
        S3Event.S3EventNotificationRecord record = event.getRecords().get(0);
        String bucket = record.getS3().getBucket().getName();
        String key = record.getS3().getObject().getKey();

        try (S3ObjectInputStream s3is = s3Client.getObject(bucket, key).getObjectContent();
             BufferedReader reader = new BufferedReader(new InputStreamReader(s3is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                // Assuming CSV format: "id,name,value"
                Map<String, AttributeValue> itemValues = new HashMap<>();
                itemValues.put("id", new AttributeValue(parts[0]));
                itemValues.put("name", new AttributeValue(parts[1]));
                itemValues.put("value", new AttributeValue(parts[2]));

                PutItemRequest putItemRequest = new PutItemRequest()
                        .withTableName("YourDynamoDBTableName")
                        .withItem(itemValues);
                dynamoDbClient.putItem(putItemRequest);
            }
        } catch (Exception e) {
            context.getLogger().log("Error processing S3 event: " + e.getMessage());
            return "Error";
        }
        return "Success";
    }

    // Inner class for AWS services module
    private static class AwsServicesModule extends com.google.inject.AbstractModule {
        @Override
        protected void configure() {
            bind(AmazonS3.class).toInstance(AmazonS3ClientBuilder.standard().build());
            bind(AmazonDynamoDB.class).toInstance(AmazonDynamoDBClientBuilder.standard().build());
        }
    }
}
