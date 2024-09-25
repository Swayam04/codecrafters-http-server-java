package http;

import java.util.HashMap;
import java.util.Map;

public class HttpHeaders {

    private final Map<String, String> headers;

    public HttpHeaders() {
        headers = new HashMap<>();
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public void addCommonHeader(CommonHeaders header, String value) {
        headers.put(header.getHeaderName(), value);
    }

    @Override
    public String toString() {
        StringBuilder serializedHeaders = new StringBuilder();
        for(Map.Entry<String, String> e : headers.entrySet()) {
            serializedHeaders.append(e.getKey()).append(": ").append(e.getValue()).append("\r\n");
        }
        return serializedHeaders.toString();
    }
}
