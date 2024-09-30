# Basic HTTP Server

This project is an implementation of a basic HTTP server, built as part of a step-by-step, unguided and TDD based [Codecrafters](https://app.codecrafters.io) challenge.

## Core Features

- **Port Binding**: Listens for incoming connections on a specified port.
- **200 OK Response**: Responds with a basic `200 OK` status.
- **URL Path Parsing**: Extracts the requested resource from incoming requests.
- **Response Body Handling**: Sends simple content in the response body.
- **HTTP Header Parsing**: Reads and processes HTTP headers.
- **Concurrent Connections**: Handles multiple requests concurrently using `ExecutorService`.
- **File Handling**: Serves static files or creates new files based on command-line input.
- **Request Body Processing**: Handles requests with a body (e.g., POST).
- **Compression**: Supports `gzip` compression for efficient content delivery.

## Object-Oriented Design & Patterns

The project follows Object-Oriented principles, utilizing:
- **Builder Pattern** for constructing request/response objects.
- **Factory Pattern** for routing different endpoint requests.

## Technology Stack

- **Java**
- **Maven**
- **SLF4J & Logback** for logging.

## Usage

### Compile

```bash
cd "$(dirname "$0")"
mvn -B package -Ddir=/path/to/build
```

### Run

```bash
java -jar /tmp/codecrafters-build-http-server-java/java_http.jar --directory <file_directory>
```

Replace `<server_port>` with the desired port and `<file_directory>` with the target directory for file handling.

## Additional Features

- **Concurrent Request Handling** via `ExecutorService`.
- **Gzip Compression** for bandwidth optimization using `GZIPOutputStream`.
- **Static File Serving** and file creation support using `java.nio.file` package in a directory passed as command-line argument.

## Future Enhancements

Planned improvements:
- **Range Requests** for partial content responses.
- **HTTP Pipelining** for handling multiple requests over a single connection.
- **E-Tag Caching** for more efficient caching mechanisms.

## Acknowledgments

This project was built as part of the Codecrafters challenge, with all implementation done independently.

---