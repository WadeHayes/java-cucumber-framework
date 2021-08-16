package net.wade.autotests.core;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.ConnectionConfig;
import com.jayway.restassured.config.DecoderConfig;
import com.jayway.restassured.config.EncoderConfig;
import com.jayway.restassured.config.HttpClientConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Headers;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.jayway.restassured.RestAssured.given;
import static org.testng.Assert.*;

@Component
public class REST {

    private final static Logger logger = Logger.getLogger(String.valueOf(REST.class));

    public REST() {
        RestAssured.useRelaxedHTTPSValidation();
        RestAssured.config = RestAssured.config().connectionConfig(ConnectionConfig.connectionConfig().closeIdleConnectionsAfterEachResponseAfter(10, TimeUnit.MILLISECONDS));
    }


    ///////////////////
    /////// GET ///////
    ///////////////////

    public Response GET(String envelopment) {
        return GET(envelopment, null, HttpResponseStatusType.OK_200.getStatusCode(), null);
    }

    public Response GET(String envelopment, int statusCode) {
        return GET(envelopment, null, statusCode, null);
    }

    public Response GET(String envelopment, int statusCode, Headers headers) {
        return GET(envelopment, null, statusCode, headers);
    }

    public Response GET(String envelopment, String responsePath, int statusCode, Headers headers) {
        String url;
        if (envelopment.startsWith("http")) {
            url = envelopment;
        } else {
            url = "" + envelopment; // дописать получение url из проперти
        }

        Response get;
        logger.info("Send get request to: " + url);

        RequestSpecification give = given();

        if (headers != null) {
            give = give.headers(headers);
        }

        get = give.get(url);

        if (get.statusCode() != statusCode) {
            fail(get.getBody().prettyPrint());
        }

        if (responsePath != null && !get.asString().isEmpty()) {
            JSON.validateJSONs(get.asString(), responsePath);
        }

        return get.andReturn();
    }


    ////////////////////
    /////// POST ///////
    ////////////////////

    public Response POST(String envelopment) {
        return POST(envelopment, null, HttpResponseStatusType.CREATED_201.getStatusCode());
    }

    public Response POST(String envelopment, int statusCode) {
        return POST(envelopment, null, statusCode);
    }

    public Response POST(String envelopment, String requestPath, int statusCode) {
        return POST(envelopment, requestPath, null, statusCode, null);
    }

    public Response POST(String envelopment, String requestPath, int statusCode, Headers headers) {
        return POST(envelopment, requestPath, null, statusCode, headers);
    }

    public Response POST(String envelopment, String requestPath, String responsePath, int statusCode, Headers headers) {
        String url;
        if (envelopment.startsWith("http")) {
            url = envelopment;
        } else {
            url = "" + envelopment; // дописать получение url из проперти
        }

        Response post;
        RequestSpecification give = given();

        if (headers != null) {
            give = give.headers(headers);
        }

        String rqs = "";
        try {
            File rq = new File(requestPath);
            if (rq.isFile()) {
                byte[] encoded = Files.readAllBytes(rq.toPath());
                rqs = new String(encoded, StandardCharsets.UTF_8);
            } else {
                rqs = requestPath;
            }
        } catch (Exception e) {

        }

        if (requestPath != null) {
            give = give.body(rqs);
        }

        logger.info("Send post request to: " + url);

        post = give.when().post(url);

        if (post.statusCode() != statusCode) {
            fail(post.getBody().prettyPrint());
        }

        if (responsePath != null && !post.asString().isEmpty()) {
            JSON.validateJSONs(post.asString(), responsePath);
        }

        return post.andReturn();
    }

    public Response POST(String envelopment, Map<String, Object> multiPart, int statusCode, Headers headers, String charset) {
        String url;
        if (envelopment.startsWith("http")) {
            url = envelopment;
        } else {
            url = "" + envelopment; // дописать получение url из проперти
        }

        Response post;
        RequestSpecification give = given();

        give.header("Content-Type", "multipart/form-data");
        if (headers != null) {
            give = give.headers(headers);
        }

        for (Map.Entry<String, Object> entry : multiPart.entrySet()) {
            give.multiPart(entry.getKey(), entry.getValue());
        }

        give.config(
                RestAssured.config()
                        .encoderConfig(EncoderConfig.encoderConfig().encodeContentTypeAs("multipart/form-data", ContentType.TEXT))
                        .httpClient(HttpClientConfig.httpClientConfig().httpMultipartMode(HttpMultipartMode.BROWSER_COMPATIBLE))
                        .encoderConfig(EncoderConfig.encoderConfig().defaultContentCharset(charset))
                        .encoderConfig(EncoderConfig.encoderConfig().defaultCharsetForContentType(charset, "multipart/form-data"))
                        .decoderConfig(DecoderConfig.decoderConfig().defaultContentCharset(charset))
                        .decoderConfig(DecoderConfig.decoderConfig().defaultCharsetForContentType(charset, "multipart/form-data"))
        );

        logger.info("Send post request to: " + url);

        post = give.when().post(url);

        if (post.statusCode() != statusCode) {
            fail(post.getBody().prettyPrint());
        }

        return post.andReturn();
    }


    /////////////////////
    /////// PATCH ///////
    /////////////////////

    public Response PATCH(String envelopment) {
        return PATCH(envelopment, null, null, HttpResponseStatusType.NO_CONTENT_204.getStatusCode());
    }

    public Response PATCH(String envelopment, int statusCode) {
        return PATCH(envelopment, null, null, statusCode);
    }

    public Response PATCH(String envelopment, String requestPath, int statusCode) {
        return PATCH(envelopment, requestPath, null, statusCode);
    }

    public Response PATCH(String envelopment, String requestPath, String responsePath, int statusCode) {
        String url;
        if (envelopment.startsWith("http")) {
            url = envelopment;
        } else {
            url = "" + envelopment; // дописать получение url из проперти
        }

        Response patch;
        RequestSpecification give = given();


        String rqs = "";
        try {
            File rq = new File(requestPath);
            if (rq.isFile()) {
                byte[] encoded = Files.readAllBytes(rq.toPath());
                rqs = new String(encoded, StandardCharsets.UTF_8);
            } else {
                rqs = requestPath;
            }
        } catch (Exception e) {

        }

        if (requestPath != null) {
            give = give.body(rqs);
        }

        logger.info("Send post request to: " + url);

        patch = give.when().post(url);

        if (patch.statusCode() != statusCode) {
            fail(patch.getBody().prettyPrint());
        }

        if (responsePath != null && !patch.asString().isEmpty()) {
            JSON.validateJSONs(patch.asString(), responsePath);
        }

        return patch.andReturn();
    }


    ///////////////////
    /////// PUT ///////
    ///////////////////

    public Response PUT(String envelopment) {
        return PUT(envelopment, null, null, HttpResponseStatusType.CREATED_201.getStatusCode());
    }

    public Response PUT(String envelopment, int statusCode) {
        return PUT(envelopment, null, null, statusCode);
    }

    public Response PUT(String envelopment, String requestPath, int statusCode) {
        return PUT(envelopment, requestPath, null, statusCode);
    }

    public Response PUT(String envelopment, String requestPath, String responsePath, int statusCode) {
        String url;
        if (envelopment.startsWith("http")) {
            url = envelopment;
        } else {
            url = "" + envelopment; // дописать получение url из проперти
        }

        Response put;
        RequestSpecification give = given();


        String rqs = "";
        try {
            File rq = new File(requestPath);
            if (rq.isFile()) {
                byte[] encoded = Files.readAllBytes(rq.toPath());
                rqs = new String(encoded, StandardCharsets.UTF_8);
            } else {
                rqs = requestPath;
            }
        } catch (Exception e) {

        }

        if (requestPath != null) {
            give = give.body(rqs);
        }

        logger.info("Send PUT request to: " + url);

        put = give.when().post(url);

        if (put.statusCode() != statusCode) {
            fail(put.getBody().prettyPrint());
        }

        if (responsePath != null && !put.asString().isEmpty()) {
            JSON.validateJSONs(put.asString(), responsePath);
        }

        return put.andReturn();
    }


    //////////////////////
    /////// DELETE ///////
    //////////////////////

    public Response DELETE(String envelopment) {
        return DELETE(envelopment, null, null, HttpResponseStatusType.CREATED_201.getStatusCode());
    }

    public Response DELETE(String envelopment, int statusCode) {
        return DELETE(envelopment, null, null, statusCode);
    }

    public Response DELETE(String envelopment, String requestPath, int statusCode) {
        return DELETE(envelopment, requestPath, null, statusCode);
    }

    public Response DELETE(String envelopment, String requestPath, String responsePath, int statusCode) {
        String url;
        if (envelopment.startsWith("http")) {
            url = envelopment;
        } else {
            url = "" + envelopment; // дописать получение url из проперти
        }

        ConnectionConfig connectionConfig = new ConnectionConfig();
        connectionConfig.closeIdleConnectionsAfterEachResponse();

        Response delete;
        RequestSpecification give = given();

        String rqs = "";
        try {
            File rq = new File(requestPath);
            if (rq.isFile()) {
                byte[] encoded = Files.readAllBytes(rq.toPath());
                rqs = new String(encoded, StandardCharsets.UTF_8);
            } else {
                rqs = requestPath;
            }
        } catch (Exception e) {

        }

        if (requestPath != null) {
            give = give.body(rqs);
        }

        logger.info("Send PUT request to: " + url);

        delete = give.when().post(url);

        if (delete.statusCode() != statusCode) {
            fail(delete.getBody().prettyPrint());
        }

        if (responsePath != null && !delete.asString().isEmpty()) {
            JSON.validateJSONs(delete.asString(), responsePath);
        }

        return delete.andReturn();
    }
}
