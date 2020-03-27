package com.amazonaws.handler;

import com.amazonaws.dao.BookingDao;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.MembersInjector;
import javax.annotation.Generated;
import javax.inject.Provider;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class GetBookingHandler_MembersInjector implements MembersInjector<GetBookingHandler> {
  private final Provider<ObjectMapper> objectMapperProvider;

  private final Provider<BookingDao> bookingDaoProvider;

  public GetBookingHandler_MembersInjector(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    this.objectMapperProvider = objectMapperProvider;
    this.bookingDaoProvider = bookingDaoProvider;
  }

  public static MembersInjector<GetBookingHandler> create(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    return new GetBookingHandler_MembersInjector(objectMapperProvider, bookingDaoProvider);
  }

  @Override
  public void injectMembers(GetBookingHandler instance) {
    injectObjectMapper(instance, objectMapperProvider.get());
    injectBookingDao(instance, bookingDaoProvider.get());
  }

  public static void injectObjectMapper(GetBookingHandler instance, ObjectMapper objectMapper) {
    instance.objectMapper = objectMapper;
  }

  public static void injectBookingDao(GetBookingHandler instance, BookingDao bookingDao) {
    instance.bookingDao = bookingDao;
  }
}
