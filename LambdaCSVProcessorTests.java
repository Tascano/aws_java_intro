package com.example.lambda;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class LambdaCSVProcessorTest {

    @Mock
    private AmazonS3 mockS3Client;
    @Mock
    private AmazonDynamoDB mockDynamoDbClient;
    @Mock
    private S3Event mockS3Event;
    @Mock
    private S3Event.S3EventNotificationRecord mockRecord;
    @Mock
    private S3Event.S3Entity mockS3Entity;
    @Mock
    private S3Event.S3ObjectEntity mockObjectEntity;

    private LambdaCSVProcessor lambdaCSVProcessor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(mockRecord.getS3()).thenReturn(mockS3Entity);
        when(mockS3Entity.getObject()).thenReturn(mockObjectEntity);
        when(mockObjectEntity.getKey()).thenReturn("test.csv");
        lambdaCSVProcessor = new LambdaCSVProcessor(mockS3Client, mockDynamoDbClient);
    }

    @Test
    public void testHandleRequest() {
        // Mock S3 object retrieval
        S3Object mockS3Object = mock(S3Object.class);
        S3ObjectInputStream mockInputStream = new S3ObjectInputStream(new ByteArrayInputStream("1,test,10\n2,example,20".getBytes()), null);
        when(mockS3Client.getObject(anyString(), anyString())).thenReturn(mockS3Object);
        when(mockS3Object.getObjectContent()).thenReturn(mockInputStream);
        when(mockS3Event.getRecords()).thenReturn(Collections.singletonList(mockRecord));

        // Call the handleRequest method
        lambdaCSVProcessor.handleRequest(mockS3Event, null);

        // Verify interactions with mockS3Client and mockDynamoDbClient
        verify(mockS3Client).getObject(anyString(), anyString());
        verify(mockDynamoDbClient, times(2)).putItem(any(PutItemRequest.class));
    }
}
