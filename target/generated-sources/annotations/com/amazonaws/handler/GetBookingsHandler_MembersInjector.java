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
public final class GetBookingsHandler_MembersInjector
    implements MembersInjector<GetBookingsHandler> {
  private final Provider<ObjectMapper> objectMapperProvider;

  private final Provider<BookingDao> bookingDaoProvider;

  public GetBookingsHandler_MembersInjector(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    this.objectMapperProvider = objectMapperProvider;
    this.bookingDaoProvider = bookingDaoProvider;
  }

  public static MembersInjector<GetBookingsHandler> create(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    return new GetBookingsHandler_MembersInjector(objectMapperProvider, bookingDaoProvider);
  }

  @Override
  public void injectMembers(GetBookingsHandler instance) {
    injectObjectMapper(instance, objectMapperProvider.get());
    injectBookingDao(instance, bookingDaoProvider.get());
  }

  public static void injectObjectMapper(GetBookingsHandler instance, ObjectMapper objectMapper) {
    instance.objectMapper = objectMapper;
  }

  public static void injectBookingDao(GetBookingsHandler instance, BookingDao bookingDao) {
    instance.bookingDao = bookingDao;
  }
}
