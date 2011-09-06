package com.rackspace.papi.filter.logic;

import com.rackspace.papi.commons.util.http.HttpStatusCode;
import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletRequest;
import com.rackspace.papi.commons.util.servlet.http.MutableHttpServletResponse;
import java.io.OutputStream;
import java.io.PrintWriter;

public class AbstractFilterDirector implements FilterDirector {

    private static final String NOT_SUPPORTED_MESSAGE = "This FilterDirector method is not supported";

    @Override
    public void applyTo(MutableHttpServletRequest request, MutableHttpServletResponse response) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public void setRequestUri(String newUri) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public void setRequestUrl(StringBuffer newUrl) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public String getResponseMessageBody() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public OutputStream getResponseOutputStream() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public PrintWriter getResponseWriter() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public FilterAction getFilterAction() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public HttpStatusCode getResponseStatus() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public HeaderManager requestHeaderManager() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public HeaderManager responseHeaderManager() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public byte[] getResponseMessageBodyBytes() {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public void setFilterAction(FilterAction action) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }

    @Override
    public void setResponseStatus(HttpStatusCode delegatedStatus) {
        throw new UnsupportedOperationException(NOT_SUPPORTED_MESSAGE);
    }
}
