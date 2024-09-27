package utils;

import http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestParser {
    private static final Logger logger = LoggerFactory.getLogger(HttpRequestParser.class);

    public static HttpRequest parse(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isEmpty()) {
            throw new IOException("Invalid HTTP request: Empty request line");
        }
        String[] statusLine = requestLine.split(" ", 3);
        if (statusLine.length != 3) {
            throw new IOException("Invalid HTTP request: Malformed request line");
        }
        String method = statusLine[0];
        String path = statusLine[1];
        String version = statusLine[2];

        logger.info("Processing request: {} {}", method, path);

        HttpRequest.RequestBuilder builder = new HttpRequest.RequestBuilder()
                .method(method)
                .path(path)
                .version(version);

        String headerLine;
        Map<String, String> headers = new HashMap<>();
        while ((headerLine = reader.readLine()) != null && !headerLine.isEmpty()) {
            int index = headerLine.indexOf(':');
            if (index > 0) {
                String key = headerLine.substring(0, index).trim();
                String value = headerLine.substring(index + 1).trim();
                headers.put(key, value);
            }
        }
        builder.addHeaders(headers);

        if (headers.containsKey("Content-Length")) {
            int contentLength = Integer.parseInt(headers.get("Content-Length"));
            char[] buffer = new char[contentLength];
            int charsRead = reader.read(buffer, 0, contentLength);
            if (charsRead == contentLength) {
                builder.body(new String(buffer));
            } else {
                throw new IOException("Incomplete body: expected " + contentLength + " chars, got " + charsRead);
            }
        }

        return builder.build();
    }

}
