package http;

public class HttpResponse {

    private final String version;
    private HttpStatus status;
    private HttpHeaders headers;
    private String body;
    private final HttpRequest request;

    public HttpResponse(HttpRequest request) {
        this.request = request;
        this.version = request.getVersion();
        setStatus();
        if(request.getPath().equals("/user-agent")) {
            setHeaders();
            setBody();
        }
    }

    private void setStatus() {
        if(request.getPath().equals("/") || request.getPath().startsWith("/echo") || request.getPath().equals("/user-agent")) {
            status = HttpStatus.OK;
        } else {
            status = HttpStatus.NOT_FOUND;
        }
    }

    private void setHeaders() {
        headers = new HttpHeaders();
        headers.addCommonHeader(CommonHeaders.CONTENT_TYPE, "text/plain");
        headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(request.getHeaders().get("User-Agent").length()));
    }

    private void setBody() {
        body = request.getHeaders().get("User-Agent");
    }

    public String respond() {
        StringBuilder response = new StringBuilder();
        response.append(version).append(" ").append(status.toString()).append("\r\n");
        if (headers != null && !headers.toString().isEmpty()) {
            response.append(headers.toString());
        }
        response.append("\r\n");
        if (body != null && !body.isEmpty()) {
            response.append(body);
        }
        return response.toString();
    }

}
