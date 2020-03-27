/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.amazonaws.handler;

import com.amazonaws.config.DaggerBookingComponent;
import com.amazonaws.config.BookingComponent;
import com.amazonaws.dao.BookingDao;
import com.amazonaws.model.BookingPage;
import com.amazonaws.model.response.GatewayResponse;
import com.amazonaws.model.response.GetBookingsResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import javax.inject.Inject;

public class GetBookingsHandler implements BookingRequestStreamHandler {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Inject
    ObjectMapper objectMapper;
    @Inject
    BookingDao bookingDao;
    private final BookingComponent bookingComponent;

    public GetBookingsHandler() {
        bookingComponent = DaggerBookingComponent.builder().build();
        bookingComponent.inject(this);
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output,
                              Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        final JsonNode event;
        try {
            event = objectMapper.readTree(input);
            logger.log("\nREQUEST: " + event);
        } catch (JsonMappingException e) {
            writeInvalidJsonInStreamResponse(objectMapper, output, e.getMessage());
            return;
        }
        final JsonNode queryParameterMap = event.findValue("queryParameters");
        final String exclusiveStartKeyQueryParameter = Optional.ofNullable(queryParameterMap)
                .map(mapNode -> mapNode.get("exclusive_start_key").asText())
                .orElse(null);

        BookingPage page = bookingDao.getBookings(exclusiveStartKeyQueryParameter);
        //TODO handle exceptions
        objectMapper.writeValue(output, new GatewayResponse<>(
                objectMapper.writeValueAsString(
                        new GetBookingsResponse(page.getLastEvaluatedKey(), page.getBookings())),
                APPLICATION_JSON, SC_OK));
    }
}
