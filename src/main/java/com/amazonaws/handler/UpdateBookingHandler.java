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
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToUpdateException;
import com.amazonaws.model.Booking;
import com.amazonaws.model.request.UpdateBookingRequest;
import com.amazonaws.model.response.ErrorMessage;
import com.amazonaws.model.response.GatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.inject.Inject;

public class UpdateBookingHandler implements BookingRequestStreamHandler {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Inject
    ObjectMapper objectMapper;
    @Inject
    BookingDao bookingDao;
    private final BookingComponent bookingComponent;

    public UpdateBookingHandler() {
        bookingComponent = DaggerBookingComponent.builder().build();
        bookingComponent.inject(this);
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output,
                              Context context) throws IOException {
        final JsonNode event;
        try {
            event = objectMapper.readTree(input);
        } catch (JsonMappingException e) {
            writeInvalidJsonInStreamResponse(objectMapper, output, e.getMessage());
            return;
        }
        if (event == null) {
            writeInvalidJsonInStreamResponse(objectMapper, output, "event was null");
            return;
        }
        final JsonNode pathParameterMap = event.findValue("pathParameters");
        final String bookingId = Optional.ofNullable(pathParameterMap)
                .map(mapNode -> mapNode.get("booking_id"))
                .map(JsonNode::asText)
                .orElse(null);
        if (isNullOrEmpty(bookingId)) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(BOOKING_ID_WAS_NOT_SET),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        JsonNode updateBookingRequestBody = event.findValue("body");
        if (updateBookingRequestBody == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage("Body was null",
                                            SC_BAD_REQUEST)),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        final UpdateBookingRequest request;
        try {
            request = objectMapper.readValue(
                    updateBookingRequestBody.asText(), UpdateBookingRequest.class);
        } catch (JsonParseException | JsonMappingException e) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage("Invalid JSON in body: "
                                            + e.getMessage(), SC_BAD_REQUEST)),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        if (request == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(REQUEST_WAS_NULL_ERROR),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        try {
            request.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).format(dateTimeFormatter));

            Booking updatedBooking = bookingDao.updateBooking(
                    Booking.builder()
                            .id(bookingId)
                            .customer(request.getCustomer())
                            .checkedIn(request.getCheckedIn())
                            .status(String.valueOf(request.getStatus()))
                            .updatedAt((request.getUpdatedAt()))
                            .build());

            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(updatedBooking),
                    APPLICATION_JSON, SC_OK));
        } catch (UnableToUpdateException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_CONFLICT)),
                    APPLICATION_JSON, SC_CONFLICT));
        } catch (TableDoesNotExistException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_BAD_REQUEST)),
                    APPLICATION_JSON, SC_BAD_REQUEST));
        } catch (IllegalArgumentException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_BAD_REQUEST)),
                    APPLICATION_JSON, SC_BAD_REQUEST));
        } catch (IllegalStateException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_INTERNAL_SERVER_ERROR)),
                    APPLICATION_JSON, SC_INTERNAL_SERVER_ERROR));
        }
    }
}
