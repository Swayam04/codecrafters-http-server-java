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
        setHeaders();
        setBody();
    }

    private void setStatus() {
        if(request.getPath().equals("/") || request.getPath().equals("/user-agent") || request.getPath().startsWith("/echo")) {
            status = HttpStatus.OK;
        } else {
            status = HttpStatus.NOT_FOUND;
        }
    }

    private void setHeaders() {
        if(request.getPath().equals("/user-agent") || request.getPath().startsWith("/echo")) {
            headers = new HttpHeaders();
            headers.addCommonHeader(CommonHeaders.CONTENT_TYPE, "text/plain");
            if(request.getPath().startsWith("/user-agent")) {
                headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(request.getHeaders().get("User-Agent").length()));
            } else {
                headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(request.getPath().substring(6).length()));
            }
        }
    }

    private void setBody() {
        if(request.getPath().startsWith("/user-agent")) {
            body = request.getHeaders().get("User-Agent");
        } else if(request.getPath().startsWith("/echo")) {
            body = request.getPath().substring(6);
        }
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
