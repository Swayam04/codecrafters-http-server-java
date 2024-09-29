package http;

import java.util.Map;
import java.util.Optional;

public class HttpRequest {

    private final String method;
    private final String path;
    private final String version;
    private final HttpHeaders headers;
    private final Optional<String> body;

    public HttpRequest(RequestBuilder requestBuilder) {
        method = requestBuilder.method;
        path = requestBuilder.path;
        version = requestBuilder.version;
        headers = requestBuilder.headers;
        body = Optional.ofNullable(requestBuilder.body);
    }

    public Optional<String> getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers.getHeaders();
    }

    public String getVersion() {
        return version;
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public static class RequestBuilder {
        private String method;
        private String path;
        private String version;
        private final HttpHeaders headers = new HttpHeaders();
        private String body;

        public RequestBuilder method(String method) {
            this.method = method;
            return this;
        }

        public RequestBuilder path(String path) {
            this.path = path;
            return this;
        }

        public RequestBuilder version(String version) {
            this.version = version;
            return this;
        }

        public RequestBuilder addHeaders(Map<String, String> headers) {
            this.headers.addAllHeaders(headers);
            return this;
        }

        public RequestBuilder body(String body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

}
