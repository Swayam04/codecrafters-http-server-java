package http;

import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private final String version;
    private HttpStatus status;
    private HttpHeaders headers;
    private String body;
    private final HttpRequest request;

    private static final Map<String, ResponseHandler> responseHandlers = new HashMap<>();

    static {
        responseHandlers.put("/", new RootHandler());
        responseHandlers.put("/echo/", new EchoHandler());
        responseHandlers.put("/user-agent", new UserAgentHandler());
    }

    public HttpResponse(HttpRequest request) {
        this.request = request;
        this.version = request.getVersion();
        processRequest();
    }

    private void processRequest() {
        ResponseHandler responseHandler = responseHandlers.entrySet().stream()
                .filter(entry -> {
                    String key = entry.getKey();
                    String path = request.getPath();
                    if (key.equals("/")) {
                        return path.equals("/");
                    } else if (key.endsWith("/")) {
                        return path.startsWith(key);
                    } else {
                        return path.equals(key);
                    }
                })
                .findAny()
                .map(Map.Entry::getValue)
                .orElse(new NotFoundHandler());
        responseHandler.handle(this);
    }

    private HttpRequest getRequest() {
        return request;
    }

    public int getStatus() {
        return status.getCode();
    }

    private void setStatus(HttpStatus status) {
        this.status = status;
    }

    private void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    private void setBody(String body) {
        this.body = body;
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

    private interface ResponseHandler {
        void handle(HttpResponse response);
    }

    private static class RootHandler implements ResponseHandler {

        @Override
        public void handle(HttpResponse response) {
            response.setStatus(HttpStatus.OK);
        }
    }

    private static class UserAgentHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            HttpRequest request = response.getRequest();
            String userAgent = request.getHeaders().get("User-Agent");
            response.setStatus(HttpStatus.OK);
            response.setHeaders(new HttpHeaders());
            response.headers.addCommonHeader(CommonHeaders.CONTENT_TYPE, "text/plain");
            response.headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(userAgent.length()));
            response.setBody(userAgent);
        }
    }

    private static class EchoHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            HttpRequest request = response.getRequest();
            String echoString = request.getPath().substring(6);
            response.setStatus(HttpStatus.OK);
            response.setHeaders(new HttpHeaders());
            response.headers.addCommonHeader(CommonHeaders.CONTENT_TYPE, "text/plain");
            response.headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(echoString.length()));
            response.setBody(echoString);
        }
    }

    private static class NotFoundHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            response.setStatus(HttpStatus.NOT_FOUND);
        }
    }

}
