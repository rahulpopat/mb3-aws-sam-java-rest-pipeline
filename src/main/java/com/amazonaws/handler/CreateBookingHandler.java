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

import com.amazonaws.config.BookingComponent;
import com.amazonaws.config.DaggerBookingComponent;
import com.amazonaws.dao.BookingDao;
import com.amazonaws.exception.CouldNotCreateBookingException;
import com.amazonaws.model.Booking;
import com.amazonaws.model.request.CreateBookingRequest;
import com.amazonaws.model.response.ErrorMessage;
import com.amazonaws.model.response.GatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import javax.inject.Inject;

public class CreateBookingHandler implements BookingRequestStreamHandler {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final ErrorMessage ERROR_REQUIRED_FLIGHT_ID
            = new ErrorMessage("Require flight information to create an booking", SC_BAD_REQUEST);
    private static final ErrorMessage ERROR_REQUIRED_PAYMENT_TOKEN
            = new ErrorMessage("Require payment token to create an booking",
            SC_BAD_REQUEST);
    private static final ErrorMessage ERROR_REQUIRED_BOOKING_STATUS
            = new ErrorMessage("Require booking status to create an booking",
            SC_BAD_REQUEST);

    @Inject
    ObjectMapper objectMapper;
    @Inject
    BookingDao bookingDao;
    private final BookingComponent bookingComponent;

    public CreateBookingHandler() {
        bookingComponent = DaggerBookingComponent.builder().build();
        bookingComponent.inject(this);
    }

    @Override
    public void handleRequest(InputStream input, OutputStream output,
                              Context context) throws IOException {

        LambdaLogger logger = context.getLogger();
        logger.log("ENVIRONMENT VARIABLES: " + gson.toJson(System.getenv()));
        logger.log("CONTEXT: " + gson.toJson(context));
        final JsonNode event;
        try {
            event = objectMapper.readTree(input);
            logger.log("REQUEST: " + event);
        } catch (JsonMappingException e) {
            writeInvalidJsonInStreamResponse(objectMapper, output, e.getMessage());
            return;
        }

        if (event == null) {
            writeInvalidJsonInStreamResponse(objectMapper, output, "event was null");
            return;
        }
        JsonNode createBookingRequestBody = event.findValue("body");
        if (createBookingRequestBody == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage("Body was null",
                                            SC_BAD_REQUEST)),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        final CreateBookingRequest request;

        try {
            request = objectMapper.treeToValue(
                    objectMapper.readTree(createBookingRequestBody.asText()),
                    CreateBookingRequest.class);
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

        // request validations
        if (isNullOrEmpty(request.getBookingOutboundFlightId())) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(ERROR_REQUIRED_FLIGHT_ID),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        if (request.getPaymentToken() == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(ERROR_REQUIRED_PAYMENT_TOKEN),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        if (request.getStatus() == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(ERROR_REQUIRED_BOOKING_STATUS),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        try {
            request.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC).format(dateTimeFormatter));
            request.setUpdatedAt(LocalDateTime.now(ZoneOffset.UTC).format(dateTimeFormatter));
            request.setBookingReference(generateAlphaNumericString(5));
            request.set__typename("Booking");
            // create entry in DynamoDb table
            final Booking booking = bookingDao.createBooking(request);
            objectMapper.writeValue(output,
                    new GatewayResponse<>(objectMapper.writeValueAsString(booking),
                            APPLICATION_JSON, SC_CREATED)); //TODO redirect with a 303
        } catch (CouldNotCreateBookingException e) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage(e.getMessage(),
                                            SC_INTERNAL_SERVER_ERROR)),
                            APPLICATION_JSON, SC_INTERNAL_SERVER_ERROR));
        }
    }

    private String generateAlphaNumericString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString.toUpperCase();
    }
}
