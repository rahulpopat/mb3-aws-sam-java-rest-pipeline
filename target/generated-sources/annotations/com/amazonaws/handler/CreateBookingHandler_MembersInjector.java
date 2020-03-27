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
public final class CreateBookingHandler_MembersInjector
    implements MembersInjector<CreateBookingHandler> {
  private final Provider<ObjectMapper> objectMapperProvider;

  private final Provider<BookingDao> bookingDaoProvider;

  public CreateBookingHandler_MembersInjector(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    this.objectMapperProvider = objectMapperProvider;
    this.bookingDaoProvider = bookingDaoProvider;
  }

  public static MembersInjector<CreateBookingHandler> create(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    return new CreateBookingHandler_MembersInjector(objectMapperProvider, bookingDaoProvider);
  }

  @Override
  public void injectMembers(CreateBookingHandler instance) {
    injectObjectMapper(instance, objectMapperProvider.get());
    injectBookingDao(instance, bookingDaoProvider.get());
  }

  public static void injectObjectMapper(CreateBookingHandler instance, ObjectMapper objectMapper) {
    instance.objectMapper = objectMapper;
  }

  public static void injectBookingDao(CreateBookingHandler instance, BookingDao bookingDao) {
    instance.bookingDao = bookingDao;
  }
}
