package net.wade.autotests.core;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.*;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ComponentScan(basePackages = "net.wade.autotests")
@PropertySource("/autotests.properties")
@PropertySource("/environments/autotests_${environment}.properties")
public final class AmazonS3Utils {

    @Autowired
    Environment environment;

    private static final int MAX_OBJECT_REQUEST_TRIES = 10;
    private static final String AWS_CREDENTIALS_PROPERTIES = "src/main/resources/aws.credentials.properties";

    private final AmazonS3 client;
    private String accessKey;
    private String secretKey;
    private String endpoint;
    private String region;

    public AmazonS3Utils() {
        this.client = buildAmazonS3Client(false);
    }

    public AmazonS3Utils(boolean pathStyleAccess) {
        this.client = buildAmazonS3Client(pathStyleAccess);
    }

    public AmazonS3Utils(boolean pathStyleAccess, String accessKey, String secretKey, String endpoint, String region) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
        this.region = region;
        this.client = buildAmazonS3Client(pathStyleAccess);
    }

    public AmazonS3 getClient() {
        return client;
    }

    private AmazonS3 buildAmazonS3Client(boolean pathStyleAccess) {
        AWSCredentialsProvider credentialsProvider;

        if (this.accessKey != null && this.secretKey != null) {
            BasicAWSCredentials awsCreds = new BasicAWSCredentials(this.accessKey, this.secretKey);
            credentialsProvider = new AWSStaticCredentialsProvider(awsCreds);
        } else {
            credentialsProvider = credentialsProvider();
        }

        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();
        builder.withClientConfiguration(clientConfiguration());
        builder.withCredentials(credentialsProvider);

        if (pathStyleAccess) {
            builder.withPathStyleAccessEnabled(true);
        }

        builder.withEndpointConfiguration(endpointConfiguration());

        return builder.build();
    }

    private PropertiesFileCredentialsProvider credentialsProvider() {
        return new PropertiesFileCredentialsProvider(AWS_CREDENTIALS_PROPERTIES);
    }

    private AwsClientBuilder.EndpointConfiguration endpointConfiguration() {
        if (this.endpoint != null && this.region != null) {
            return new AwsClientBuilder.EndpointConfiguration(
                    this.endpoint,
                    this.region
            );
        }
        return new AwsClientBuilder.EndpointConfiguration(
                environment.getProperty("amazons3.service.endpoint"),
                environment.getProperty("amazons3.signing.region")
        );
    }

    private ClientConfiguration clientConfiguration() {
        ClientConfiguration clientConfig = new ClientConfiguration();
        clientConfig.setProtocol(Protocol.HTTP);
        return clientConfig;
    }

    public PutObjectResult putObjectToBucket(String bucketName, String filePath) {
        File file = new File(filePath);
        return client.putObject(bucketName, file.getName(), file);
    }

    public ObjectMetadata getObjectMetadata(String bucketName, String filePath) {
        return client.getObjectMetadata(bucketName, filePath);
    }

    public void downloadObjectFromBucket(String bucketName, String path, String fileName) {
        client.getObject(
                new GetObjectRequest(bucketName, fileName),
                new File(path)
        );
    }

    public void deleteObjectFromBucket(String bucketName, String fileName) {
        client.deleteObject(bucketName, fileName);
    }

    public boolean doesVersionedObjectExists(String bucketName, String key, String version) {
        ObjectMetadata objectMetadata;
        for (int i = 0; i < MAX_OBJECT_REQUEST_TRIES; i++) {
            objectMetadata = client.getObjectMetadata(new GetObjectMetadataRequest(bucketName, key, version));
            if (!objectMetadata.getRawMetadata().isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
