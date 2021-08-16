package net.wade.autotests.core;

public enum HttpResponseStatusType {

    OK_200(200),
    CREATED_201(201),
    ACCEPTED(202),
    NO_CONTENT_204(204),
    MOVED_301(301),
    FOUND_302(302),
    REDIRECT_307(307),
    BAD_REQUEST_400(400),
    UNAUTHORIZED_401(401),
    FORBIDDEN_403(403),
    NOT_FOUND_404(404),
    METHOD_NOT_ALLOWED_405(405),
    NOT_ACCEPTABLE_406(406),
    UNSUPPORTED_MEDIA_FILE_415(415),
    INTERNAL_SERVER_ERROR_500(500),
    NOT_IMPLEMENTED_501(501);

    private final int statusCode;

    HttpResponseStatusType(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
