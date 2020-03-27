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

package com.amazonaws.dao;

import com.amazonaws.exception.CouldNotCreateBookingException;
import com.amazonaws.exception.BookingDoesNotExistException;
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToDeleteException;
import com.amazonaws.exception.UnableToUpdateException;
import com.amazonaws.model.Booking;
import com.amazonaws.model.BookingPage;
import com.amazonaws.model.request.CreateBookingRequest;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BookingDao {

    private static final String UPDATE_EXPRESSION
            = "SET customer = :customer, checkedIn = :checkedIn, #reservedKeywordStatus = :status, updatedAt = :updatedAt";
    private static final String ERROR_CREATE_MSG = "Error in create request in BookingDao";
    private static final String ERROR_UPDATE_MSG = "Error in update request in BookingDao";

    public static final String BOOKING_ID = "id";
    private static final String INVALID_FLIGHT_ID = "Invalid Flight Id";

    private final String tableName;
    private final DynamoDbClient dynamoDbClient;
    private final int pageSize;

    /**
     * Constructs an BookingDao.
     * @param dynamoDbClient dynamodb client
     * @param tableName name of table to use for bookings
     * @param pageSize size of pages for getBookings
     */
    public BookingDao(final DynamoDbClient dynamoDbClient, final String tableName,
                      final int pageSize) {
        this.dynamoDbClient = dynamoDbClient;
        this.tableName = tableName;
        this.pageSize = pageSize;
    }

    /**
     * Returns an booking or throws if the booking does not exist.
     * @param bookingId id of booking to get
     * @return the booking if it exists
     * @throws BookingDoesNotExistException if the booking does not exist
     */
    public Booking getBooking(final String bookingId) {
//        return Booking.builder().bookingId(bookingId).build();
        try {
            return Optional.ofNullable(
                    dynamoDbClient.getItem(GetItemRequest.builder()
                            .tableName(tableName)
                            .key(Collections.singletonMap(BOOKING_ID,
                                    AttributeValue.builder().s(bookingId).build()))
                            .build()))
                    .map(GetItemResponse::item)
                    .map(this::convert)
                    .orElseThrow(() -> new BookingDoesNotExistException("Booking "
                            + bookingId + " does not exist"));
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Booking table " + tableName + " does not exist");
        }
    }

    /**
     * Gets a page of bookings, at most pageSize long.
     * @param lastEvaluatedKey the exclusive start id for the next page.
     * @return a page of bookings.
     * @throws TableDoesNotExistException if the booking table does not exist
     */
    public BookingPage getBookings(final String lastEvaluatedKey) {
        final ScanResponse result;

        try {
            ScanRequest.Builder scanBuilder = ScanRequest.builder()
                    .tableName(tableName)
                    .limit(pageSize);
            if (!isNullOrEmpty(lastEvaluatedKey)) {
                scanBuilder.exclusiveStartKey(Collections.singletonMap(BOOKING_ID,
                        AttributeValue.builder().s(lastEvaluatedKey).build()));
            }
            result = dynamoDbClient.scan(scanBuilder.build());
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Booking table " + tableName
                    + " does not exist");
        }

        final List<Booking> bookings = result.items().stream()
                .map(this::convert)
                .collect(Collectors.toList());

        BookingPage.BookingPageBuilder builder = BookingPage.builder().bookings(bookings);
        if (result.lastEvaluatedKey() != null && !result.lastEvaluatedKey().isEmpty()) {
            if ((!result.lastEvaluatedKey().containsKey(BOOKING_ID)
                    || isNullOrEmpty(result.lastEvaluatedKey().get(BOOKING_ID).s()))) {
                throw new IllegalStateException(
                    "bookingId did not exist or was not a non-empty string in the lastEvaluatedKey");
            } else {
                builder.lastEvaluatedKey(result.lastEvaluatedKey().get(BOOKING_ID).s());
            }
        }

        return builder.build();
    }

    /**
     * Updates an booking object.
     * @param booking booking to update
     * @return updated booking
     */
    public Booking updateBooking(final Booking booking) {
        if (booking == null) {
            throw new IllegalArgumentException("Booking to update was null");
        }
        String bookingId = booking.getId();
        if (isNullOrEmpty(bookingId)) {
            throw new IllegalArgumentException("bookingId was null or empty");
        }

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        Map<String, String> expressionAttributeNames = new HashMap<>();
        try {
            expressionAttributeValues.put(":customer",
                    AttributeValue.builder().s(booking.getCustomer()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_UPDATE_MSG);
        }
        try {
            expressionAttributeValues.put(":checkedIn",
                    AttributeValue.builder().bool(booking.getCheckedIn()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_UPDATE_MSG);
        }
        try {
            expressionAttributeValues.put(":status",
                    AttributeValue.builder().s(booking.getStatus()).build());
            expressionAttributeNames.put("#reservedKeywordStatus", "status");
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_UPDATE_MSG);
        }
        try {
            expressionAttributeValues.put(":updatedAt",
                    AttributeValue.builder().s(booking.getUpdatedAt()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_UPDATE_MSG);
        }

        final UpdateItemResponse result;
        try {
            result = dynamoDbClient.updateItem(UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(Collections.singletonMap(BOOKING_ID,
                            AttributeValue.builder().s(booking.getId()).build()))
                    .returnValues(ReturnValue.ALL_NEW)
                    .updateExpression(UPDATE_EXPRESSION)
                    .expressionAttributeNames(expressionAttributeNames)
                    .expressionAttributeValues(expressionAttributeValues)
                    .conditionExpression("attribute_exists(id)")
                    .build());
        } catch (ConditionalCheckFailedException e) {
            throw new UnableToUpdateException(
                    "Booking did not exist");
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Booking table " + tableName
                    + " does not exist and was deleted after reading the booking");
        }
        return convert(result.attributes());
    }

    /**
     * Deletes an booking.
     * @param bookingId booking id of booking to delete
     * @return the deleted booking
     */
    public Booking deleteBooking(final String bookingId) {
        final DeleteItemResponse result;
        try {
            return Optional.ofNullable(dynamoDbClient.deleteItem(DeleteItemRequest.builder()
                            .tableName(tableName)
                            .key(Collections.singletonMap(BOOKING_ID,
                                    AttributeValue.builder().s(bookingId).build()))
                            .conditionExpression("attribute_exists(id)")
                            .returnValues(ReturnValue.ALL_OLD)
                            .build()))
                    .map(DeleteItemResponse::attributes)
                    .map(this::convert)
                    .orElseThrow(() -> new IllegalStateException(
                            "Condition passed but deleted item was null"));
        } catch (ConditionalCheckFailedException e) {
            throw new UnableToDeleteException(
                    "A competing request changed the booking while processing this request");
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Booking table " + tableName
                    + " does not exist and was deleted after reading the booking");
        }
    }

    private Booking convert(final Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        Booking.BookingBuilder builder = Booking.builder();

        try {
            builder.id(item.get(BOOKING_ID).s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid bookingId attribute");
        }

        try {
            builder.bookingOutboundFlightId(item.get("bookingOutboundFlightId").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid bookingOutboundFlightId attribute");
        }

        try {
            builder.checkedIn(item.get("checkedIn").bool());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid checkedIn attribute");
        }

        try {
            builder.customer(item.get("customer").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid customer attribute");
        }

        try {
            builder.paymentToken(item.get("paymentToken").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid paymentToken attribute");
        }

        try {
            builder.status(item.get("status").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid status attribute");
        }

        try {
            builder.createdAt(item.get("createdAt").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid createdAt attribute");
        }

        try {
            builder.updatedAt(item.get("updatedAt").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid updatedAt attribute");
        }

        try {
            builder.bookingReference(item.get("bookingReference").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "Invalid bookingReference attribute");
        }

        return builder.build();
    }

    private Map<String, AttributeValue> createBookingItem(final CreateBookingRequest booking) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(BOOKING_ID, AttributeValue.builder().s(UUID.randomUUID().toString()).build());

        try {
            item.put("bookingOutboundFlightId",
                    AttributeValue.builder().s(booking.getBookingOutboundFlightId()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("paymentToken",
                    AttributeValue.builder().s(booking.getPaymentToken()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("customer",
                    AttributeValue.builder().s(booking.getCustomer()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("checkedIn",
                    AttributeValue.builder().bool(booking.getCheckedIn()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("status",
                    AttributeValue.builder().s(String.valueOf(booking.getStatus())).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("createdAt",
                    AttributeValue.builder().s(booking.getCreatedAt()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("updatedAt",
                    AttributeValue.builder().s(booking.getUpdatedAt()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("__typename",
                    AttributeValue.builder().s(booking.get__typename()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        try {
            item.put("bookingReference",
                    AttributeValue.builder().s(booking.getBookingReference()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(ERROR_CREATE_MSG);
        }
        return item;
    }

//    private String validateCustomerId(final String customerId) {
//        if (isNullOrEmpty(customerId)) {
//            throw new IllegalArgumentException("customerId was null or empty");
//        }
//        return customerId;
//    }

    /**
     * Creates an booking.
     * @param createBookingRequest details of booking to create
     * @return created booking
     */
    public Booking createBooking(final CreateBookingRequest createBookingRequest) {
        if (createBookingRequest == null) {
            throw new IllegalArgumentException("CreateBookingRequest was null");
        }
        int tries = 0;
        while (tries < 3) {
            try {
                Map<String, AttributeValue> item = createBookingItem(createBookingRequest);
                PutItemRequest putItemRequest = PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item)
                        .conditionExpression("attribute_not_exists(id)")
                        .build();
                dynamoDbClient.putItem(putItemRequest);
                return Booking.builder()
                        .id(item.get(BOOKING_ID).s())
                        .bookingOutboundFlightId(item.get("bookingOutboundFlightId").s())
                        .paymentToken(item.get("paymentToken").s())
                        .customer(item.get("customer").s())
                        .checkedIn(item.get("checkedIn").bool())
                        .status(item.get("status").s())
                        .createdAt(item.get("createdAt").s())
                        .updatedAt(item.get("updatedAt").s())
                        .bookingReference(item.get("bookingReference").s())
                        .build();
            } catch (ConditionalCheckFailedException e) {
                tries++;
            } catch (ResourceNotFoundException e) {
                throw new TableDoesNotExistException(
                        "Booking table " + tableName + " does not exist");
            }
        }
        throw new CouldNotCreateBookingException(
                "Unable to generate unique booking id after 10 tries");
    }

    private static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }
}

