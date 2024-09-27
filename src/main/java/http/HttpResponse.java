package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {

    private final String version;
    private HttpStatus status;
    private HttpHeaders headers;
    private String body;
    private final HttpRequest request;
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private static final Map<String, ResponseHandler> responseHandlers = new HashMap<>();

    static {
        responseHandlers.put("/", new RootHandler());
        responseHandlers.put("/echo/", new EchoHandler());
        responseHandlers.put("/user-agent", new UserAgentHandler());
        responseHandlers.put("/files/", new FileHandler());
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
            String echoString = request.getPath().substring("/echo/".length());
            response.setStatus(HttpStatus.OK);
            response.setHeaders(new HttpHeaders());
            response.headers.addCommonHeader(CommonHeaders.CONTENT_TYPE, "text/plain");
            response.headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(echoString.length()));
            response.setBody(echoString);
        }
    }

    private static class FileHandler implements ResponseHandler {
        private final Path basePath;

        public FileHandler() {
            basePath = Paths.get("/tmp/");
        }

        @Override
        public void handle(HttpResponse response) {
            HttpRequest request = response.getRequest();
            String fileName = request.getPath().substring("/files/".length());
            Path filePath = basePath.resolve(fileName).normalize();

            if(Files.exists(filePath)) {
                logger.info("File found: {}", fileName);
                try {
                    byte[] fileContent = Files.readAllBytes(filePath);
                    response.setStatus(HttpStatus.OK);
                    response.setHeaders(new HttpHeaders());
                    response.headers.addCommonHeader(CommonHeaders.CONTENT_TYPE, "application/octet-stream");
                    response.headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length));
                    response.setBody(Arrays.toString(fileContent));
                } catch (IOException e) {
                    logger.error("Error while reading file: {}", fileName, e);
                    new InternalServerErrorHandler().handle(response);
                }
            } else {
                logger.info("File not found: {}", fileName);
                new NotFoundHandler().handle(response);
            }
        }
    }

    private static class NotFoundHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            response.setStatus(HttpStatus.NOT_FOUND);
        }
    }

    private static class ForbiddenHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            response.setStatus(HttpStatus.FORBIDDEN);
        }
    }

    private static class InternalServerErrorHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
