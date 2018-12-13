/*
 * Copyright 2006-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.http.message;

import com.consol.citrus.endpoint.resolver.DynamicEndpointUriResolver;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Christoph Deppisch
 * @since 2.0
 */
public class HttpMessage extends DefaultMessage {

    /** Http cookies */
    private List<Cookie> cookies = new ArrayList<>();

    /** Query params */
    private Map<String, String> queryParams = new HashMap<>();

    /**
     * Empty constructor initializing with empty message payload.
     */
    public HttpMessage() {
        super();
    }

    /**
     * Constructs copy of given message.
     * @param message The base message for the copy operation
     */
    public HttpMessage(Message message) {
        super(message);
        copyCookies(message);
    }

    /**
     * Default message using message payload.
     * @param payload The payload for the message to set
     */
    public HttpMessage(Object payload) {
        super(payload);
    }

    /**
     * Default message using message payload and headers.
     * @param payload The payload for the message to set
     * @param headers A key value map containing the headers to set
     */
    public HttpMessage(Object payload, Map<String, Object> headers) {
        super(payload, headers);
    }

    /**
     * Sets the cookies extracted from the given message as far as it is a HttpMessage
     * @param message the message to extract the cookies from
     */
    private void copyCookies(Message message) {
        if (message instanceof HttpMessage) {
            this.cookies.addAll(((HttpMessage) message).getCookies());
        }
    }

    /**
     * Sets the Http request method header.
     * @param method The Http method header to use
     * @return The altered HttpMessage
     */
    public HttpMessage method(HttpMethod method) {
        setHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD, method.name());
        return this;
    }

    /**
     * Sets the Http version header.
     * @param version The http version header value to use
     * @return The altered HttpMessage
     */
    public HttpMessage version(String version) {
        setHeader(HttpMessageHeaders.HTTP_VERSION, version);
        return this;
    }

    /**
     * Sets the Http response status code.
     * @param statusCode The status code header to respond with
     * @return The altered HttpMessage
     */
    public HttpMessage status(HttpStatus statusCode) {
        statusCode(statusCode.value());
        reasonPhrase(statusCode.name());
        return this;
    }

    /**
     * Sets the Http response status code header.
     * @param statusCode The status code header value to respond with
     * @return The altered HttpMessage
     */
    public HttpMessage statusCode(Integer statusCode) {
        setHeader(HttpMessageHeaders.HTTP_STATUS_CODE, statusCode);
        return this;
    }

    /**
     * Sets the Http response reason phrase header.
     * @param reasonPhrase The reason phrase header value to use
     * @return The altered HttpMessage
     */
    public HttpMessage reasonPhrase(String reasonPhrase) {
        setHeader(HttpMessageHeaders.HTTP_REASON_PHRASE, reasonPhrase);
        return this;
    }

    /**
     * Sets the Http request request uri header.
     * @param requestUri The request uri header value to use
     * @return The altered HttpMessage
     */
    public HttpMessage uri(String requestUri) {
        setHeader(DynamicEndpointUriResolver.ENDPOINT_URI_HEADER_NAME, requestUri);
        setHeader(HttpMessageHeaders.HTTP_REQUEST_URI, requestUri);
        return this;
    }

    /**
     * Sets the Http request content type header.
     * @param contentType The content type header value to use
     * @return The altered HttpMessage
     */
    public HttpMessage contentType(String contentType) {
        setHeader("Content-Type", contentType);
        return this;
    }

    /**
     * Sets the Http accepted content type header for response.
     * @param accept The accept header value to set
     * @return The altered HttpMessage
     */
    public HttpMessage accept(String accept) {
        setHeader("Accept", accept);
        return this;
    }

    /**
     * Sets the Http request context path header.
     * @param contextPath The context path header value to use
     * @return The altered HttpMessage
     */
    public HttpMessage contextPath(String contextPath) {
        setHeader(HttpMessageHeaders.HTTP_CONTEXT_PATH, contextPath);
        return this;
    }

    /**
     * Sets the Http request query params query String. Query String is a compilation of key-value pairs separated
     * by comma character e.g. key1=value1[","key2=value2]. Query String can be empty.
     * @param queryParamString The query parameter string to evaluate
     * @return The altered HttpMessage
     */
    public HttpMessage queryParams(String queryParamString) {
        header(HttpMessageHeaders.HTTP_QUERY_PARAMS, queryParamString);
        header(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME, queryParamString);

        this.queryParams = Stream.of(queryParamString.split(",")).map(keyValue -> Optional.ofNullable(StringUtils.split(keyValue, "=")).orElse(new String[] {keyValue, ""}))
                                                                        .filter(keyValue -> StringUtils.hasText(keyValue[0]))
                                                                        .collect(Collectors.toMap(keyValue -> keyValue[0], keyValue -> keyValue[1]));
        return this;
    }

    /**
     * Sets a new Http request query param.
     * @param name The name of the request query parameter
     * @return The altered HttpMessage
     */
    public HttpMessage queryParam(String name) {
        return queryParam(name, null);
    }

    /**
     * Sets a new Http request query param.
     * @param name The name of the request query parameter
     * @param value The value of the request query parameter
     * @return The altered HttpMessage
     */
    public HttpMessage queryParam(String name, String value) {
        if (!StringUtils.hasText(name)) {
            throw new CitrusRuntimeException("Invalid query param name - must not be empty!");
        }

        this.queryParams.put(name, value);

        String queryParamString = queryParams.entrySet()
                                             .stream()
                                             .map(entry -> entry.getKey() + (entry.getValue() != null ? "=" + entry.getValue() : ""))
                                             .collect(Collectors.joining(","));

        header(HttpMessageHeaders.HTTP_QUERY_PARAMS, queryParamString);
        header(DynamicEndpointUriResolver.QUERY_PARAM_HEADER_NAME, queryParamString);

        return this;
    }

    /**
     * Sets request path that is dynamically added to base uri.
     * @param path The part of the path to add
     * @return The altered HttpMessage
     */
    public HttpMessage path(String path) {
        header(HttpMessageHeaders.HTTP_REQUEST_URI, path);
        header(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME, path);
        return this;
    }

    /**
     * Sets new header name value pair.
     * @param headerName The name of the header
     * @param headerValue The value of the header
     * @return The altered HttpMessage
     */
    public HttpMessage header(String headerName, Object headerValue) {
        return (HttpMessage) super.setHeader(headerName, headerValue);
    }

    @Override
    public HttpMessage setHeader(String headerName, Object headerValue) {
        return (HttpMessage) super.setHeader(headerName, headerValue);
    }

    @Override
    public HttpMessage addHeaderData(String headerData) {
        return (HttpMessage) super.addHeaderData(headerData);
    }

    /**
     * Gets the Http request method.
     * @return The used HttpMethod
     */
    public HttpMethod getRequestMethod() {
        Object method = getHeader(HttpMessageHeaders.HTTP_REQUEST_METHOD);

        if (method != null) {
            return HttpMethod.valueOf(method.toString());
        }

        return null;
    }

    /**
     * Gets the Http request request uri.
     * @return The request uri
     */
    public String getUri() {
        Object requestUri = getHeader(HttpMessageHeaders.HTTP_REQUEST_URI);

        if (requestUri != null) {
            return requestUri.toString();
        }

        return null;
    }

    /**
     * Gets the Http request context path.
     * @return the context path
     */
    public String getContextPath() {
        Object contextPath = getHeader(HttpMessageHeaders.HTTP_CONTEXT_PATH);

        if (contextPath != null) {
            return contextPath.toString();
        }

        return null;
    }

    /**
     * Gets the Http content type header.
     * @return the content type header value
     */
    public String getContentType() {
        Object contentType = getHeader(HttpMessageHeaders.HTTP_CONTENT_TYPE);

        if (contentType != null) {
            return contentType.toString();
        }

        return null;
    }

    /**
     * Gets the accept header.
     * @return The accept header value
     */
    public String getAccept() {
        Object accept = getHeader("Accept");

        if (accept != null) {
            return accept.toString();
        }

        return null;
    }

    /**
     * Gets the Http request query params.
     * @return The query parameters as a key value map
     */
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    /**
     * Gets the Http request query param string.
     * @return The query parameter as string
     */
    public String getQueryParamString() {
        return Optional.ofNullable(getHeader(HttpMessageHeaders.HTTP_QUERY_PARAMS)).map(Object::toString).orElse("");
    }

    /**
     * Gets the Http response status code.
     * @return The status code of the message
     */
    public HttpStatus getStatusCode() {
        Object statusCode = getHeader(HttpMessageHeaders.HTTP_STATUS_CODE);

        if (statusCode != null) {
            if (statusCode instanceof HttpStatus) {
                return (HttpStatus) statusCode;
            } else if (statusCode instanceof Integer) {
                return HttpStatus.valueOf((Integer) statusCode);
            } else {
                return HttpStatus.valueOf(Integer.valueOf(statusCode.toString()));
            }
        }

        return null;
    }

    /**
     * Gets the Http response reason phrase.
     * @return The reason phrase of the message
     */
    public String getReasonPhrase() {
        Object reasonPhrase = getHeader(HttpMessageHeaders.HTTP_REASON_PHRASE);

        if (reasonPhrase != null) {
            return reasonPhrase.toString();
        }

        return null;
    }

    /**
     * Gets the Http version.
     * @return The http version of the message
     */
    public String getVersion() {
        Object version = getHeader(HttpMessageHeaders.HTTP_VERSION);

        if (version != null) {
            return version.toString();
        }

        return null;
    }

    /**
     * Gets the request path after the context path.
     * @return The request path of the message
     */
    public String getPath() {
        Object path = getHeader(DynamicEndpointUriResolver.REQUEST_PATH_HEADER_NAME);

        if (path != null) {
            return path.toString();
        }

        return null;
    }

    /**
     * Gets the cookies.
     *
     * @return The list of cookies for this message
     */
    public List<Cookie> getCookies() {
        return cookies;
    }

    /**
     * Sets the cookies.
     *
     * @param cookies The cookies to set
     */
    public void setCookies(Cookie[] cookies) {
        this.cookies.clear();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookie(cookie);
            }
        }
    }

    /**
     * Adds new cookie to this http message.
     * @param cookie The Cookie to set
     * @return The altered HttpMessage
     */
    public HttpMessage cookie(Cookie cookie) {
        this.cookies.add(cookie);

        setHeader(HttpMessageHeaders.HTTP_COOKIE_PREFIX + cookie.getName(), getCookieString(cookie));

        return this;
    }

    private String getCookieString(Cookie cookie) {
        StringBuilder builder = new StringBuilder();

        builder.append(cookie.getName());
        builder.append("=");
        builder.append(cookie.getValue());

        if (cookie.getVersion() > 0) {
            builder.append(";Version=").append(cookie.getVersion());
        }

        if (StringUtils.hasText(cookie.getPath())) {
            builder.append(";Path=").append(cookie.getPath());
        }

        if (StringUtils.hasText(cookie.getDomain())) {
            builder.append(";Domain=").append(cookie.getDomain());
        }

        if (cookie.getMaxAge() > 0) {
            builder.append(";Max-Age=").append(cookie.getMaxAge());
        }

        if (StringUtils.hasText(cookie.getComment())) {
            builder.append(";Comment=").append(cookie.getComment());
        }

        if (cookie.getSecure()) {
            builder.append(";Secure=").append(cookie.getSecure());
        }

        return builder.toString();
    }

    /**
     * Reads request from complete request dump.
     * @param requestData The request dump to parse
     * @return The parsed dump as HttpMessage
     */
    public static HttpMessage fromRequestData(String requestData) {
        try (BufferedReader reader = new BufferedReader(new StringReader(requestData))) {
            HttpMessage request = new HttpMessage();

            String[] requestLine = reader.readLine().split("\\s");
            if (requestLine.length > 0) {
                request.method(HttpMethod.valueOf(requestLine[0]));
            }

            if (requestLine.length > 1) {
                request.uri(requestLine[1]);
            }

            if (requestLine.length > 2) {
                request.version(requestLine[2]);
            }

            String line = reader.readLine();
            while (StringUtils.hasText(line)) {
                if (!line.contains(":")) {
                    throw new CitrusRuntimeException(String.format("Invalid header syntax in line - expected 'key:value' but was '%s'", line));
                }

                String[] keyValue = line.split(":");
                request.setHeader(keyValue[0].trim(), keyValue[1].trim());
                line = reader.readLine();
            }

            StringBuilder bodyBuilder = new StringBuilder();
            line = reader.readLine();
            while (StringUtils.hasText(line)) {
                bodyBuilder.append(line).append(System.getProperty("line.separator"));
                line = reader.readLine();
            }

            request.setPayload(bodyBuilder.toString().trim());

            return request;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Http raw request data", e);
        }
    }

    /**
     * Reads response from complete response dump.
     * @param responseData The response dump to parse
     * @return The parsed dump as HttpMessage
     */
    public static HttpMessage fromResponseData(String responseData) {
        try (BufferedReader reader = new BufferedReader(new StringReader(responseData))) {
            HttpMessage response = new HttpMessage();

            String[] statusLine = reader.readLine().split("\\s");
            if (statusLine.length > 0) {
                response.version(statusLine[0]);
            }

            if (statusLine.length > 1) {
                response.status(HttpStatus.valueOf(Integer.valueOf(statusLine[1])));
            }

            String line = reader.readLine();
            while (StringUtils.hasText(line)) {
                if (!line.contains(":")) {
                    throw new CitrusRuntimeException(String.format("Invalid header syntax in line - expected 'key:value' but was '%s'", line));
                }

                String[] keyValue = line.split(":");
                response.setHeader(keyValue[0].trim(), keyValue[1].trim());
                line = reader.readLine();
            }

            StringBuilder bodyBuilder = new StringBuilder();
            line = reader.readLine();
            while (StringUtils.hasText(line)) {
                bodyBuilder.append(line).append(System.getProperty("line.separator"));
                line = reader.readLine();
            }

            response.setPayload(bodyBuilder.toString().trim());

            return response;
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to parse Http raw response data", e);
        }
    }
}
