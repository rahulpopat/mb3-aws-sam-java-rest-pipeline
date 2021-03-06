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
public final class DeleteBookingHandler_MembersInjector
    implements MembersInjector<DeleteBookingHandler> {
  private final Provider<ObjectMapper> objectMapperProvider;

  private final Provider<BookingDao> bookingDaoProvider;

  public DeleteBookingHandler_MembersInjector(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    this.objectMapperProvider = objectMapperProvider;
    this.bookingDaoProvider = bookingDaoProvider;
  }

  public static MembersInjector<DeleteBookingHandler> create(
      Provider<ObjectMapper> objectMapperProvider, Provider<BookingDao> bookingDaoProvider) {
    return new DeleteBookingHandler_MembersInjector(objectMapperProvider, bookingDaoProvider);
  }

  @Override
  public void injectMembers(DeleteBookingHandler instance) {
    injectObjectMapper(instance, objectMapperProvider.get());
    injectBookingDao(instance, bookingDaoProvider.get());
  }

  public static void injectObjectMapper(DeleteBookingHandler instance, ObjectMapper objectMapper) {
    instance.objectMapper = objectMapper;
  }

  public static void injectBookingDao(DeleteBookingHandler instance, BookingDao bookingDao) {
    instance.bookingDao = bookingDao;
  }
}
