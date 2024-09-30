package http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class HttpResponse {

    private final String version;
    private HttpStatus status;
    private HttpHeaders headers;
    private String body;
    private final HttpRequest request;
    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);
    private final String[] commandLineArguments;

    private static final Map<String, ResponseHandler> getResponseHandlers = new HashMap<>();
    private static final Map<String, ResponseHandler> postResponseHandlers = new HashMap<>();

    static {
        getResponseHandlers.put("/", new RootHandler());
        getResponseHandlers.put("/echo/", new EchoHandler());
        getResponseHandlers.put("/user-agent", new UserAgentHandler());
        getResponseHandlers.put("/files/", new FileHandler());
        postResponseHandlers.put("/files/", new FilesPostHandler());
    }

    public static String parseDirectoryArgument(String[] arguments) {
        for (int i = 0; i < arguments.length - 1; i++) {
            if (arguments[i].equals("--directory")) {
                return arguments[i + 1];
            }
        }
        return null;
    }

    public HttpResponse(HttpRequest request, String[] args) {
        this.request = request;
        this.version = request.getVersion();
        commandLineArguments = args;
        processRequest();
    }

    private void processRequest() {
        ResponseHandler responseHandler = request.getMethod().equals("GET") ?
                getResponseHandlers.getOrDefault(findPathKey(getResponseHandlers, request.getPath()), new NotFoundHandler()) :
                postResponseHandlers.getOrDefault(findPathKey(postResponseHandlers, request.getPath()), new BadRequestHandler());

        responseHandler.handle(this);
    }

    private String findPathKey(Map<String, ResponseHandler> handlers, String path) {
        return handlers.keySet().stream()
                .filter(key -> key.equals("/") ? path.equals("/") : path.startsWith(key))
                .findFirst()
                .orElse(null);
    }

    private HttpRequest getRequest() {
        return request;
    }

    private String[] getCommandLineArguments() {
        return commandLineArguments;
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
            if(request.getHeaders().containsKey(CommonHeaders.ACCEPT_ENCODING.getHeaderName())) {
                response.headers.addCommonHeader(CommonHeaders.CONTENT_ENCODING,
                        request.getHeaders().get(CommonHeaders.ACCEPT_ENCODING.getHeaderName()));
                echoString = compressString(echoString);
            }
            logger.info("String: {} of length: {}", echoString, echoString.length());
            response.headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(echoString.length()));
            response.setBody(echoString);
        }

        private static String compressString(String str) {
            try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
                gzipOutputStream.write(str.getBytes(StandardCharsets.UTF_8));
                gzipOutputStream.finish();
                return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
            } catch(IOException e) {
                logger.error("Error compressing string", e);
                return str;
            }
        }
    }

    private static class FileHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            HttpRequest request = response.getRequest();
            String fileName = request.getPath().substring("/files/".length());
            String baseDirectory = parseDirectoryArgument(response.getCommandLineArguments());
            if(baseDirectory == null) {
                new ForbiddenHandler().handle(response);
                return;
            }
            Path basePath = Paths.get(baseDirectory);
            Path filePath = basePath.resolve(fileName).normalize();

            logger.info("Looking for file {} in path {}", fileName, filePath);

            if(Files.exists(filePath)) {
                logger.info("File found: {}", fileName);
                try {
                    byte[] fileContent = Files.readAllBytes(filePath);
                    response.setStatus(HttpStatus.OK);
                    response.setHeaders(new HttpHeaders());
                    response.headers.addCommonHeader(CommonHeaders.CONTENT_TYPE, "application/octet-stream");
                    response.headers.addCommonHeader(CommonHeaders.CONTENT_LENGTH, String.valueOf(fileContent.length));
                    response.setBody(new String(fileContent, StandardCharsets.UTF_8));
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

    private static class BadRequestHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            response.setStatus(HttpStatus.BAD_REQUEST);
        }
    }

    public static class FilesPostHandler implements ResponseHandler {
        @Override
        public void handle(HttpResponse response) {
            HttpRequest request = response.getRequest();
            String fileName = request.getPath().substring("/files/".length());
            String contents = "";
            if(request.getBody().isPresent()) {
                contents = request.getBody().get();
            }
            String baseDirectory = parseDirectoryArgument(response.getCommandLineArguments());
            if(baseDirectory == null) {
                new ForbiddenHandler().handle(response);
                return;
            }
            Path filePath = Paths.get(baseDirectory).resolve(fileName).normalize();
            logger.info("Creating file in path: {}", filePath);

            try {
                Files.writeString(filePath, contents);
                response.setStatus(HttpStatus.CREATED);
            } catch (IOException e) {
                logger.error("Error while writing file: {}", fileName, e);
                new InternalServerErrorHandler().handle(response);
            }
        }
    }

}
