package http;

public enum CommonHeaders {
    CONTENT_TYPE("Content-Type"),
    ACCEPT("Accept"),
    USER_AGENT("User-Agent"),
    CONTENT_LENGTH("Content-Length"),
    HOST("Host"),
    ACCEPT_ENCODING("Accept-Encoding"),
    CONTENT_ENCODING("Content-Encoding");

    private final String headerName;
    CommonHeaders(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderName() {
        return headerName;
    }
}
