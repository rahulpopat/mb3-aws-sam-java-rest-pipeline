package com.amazonaws.config;

import com.amazonaws.dao.BookingDao;
import com.amazonaws.handler.CreateBookingHandler;
import com.amazonaws.handler.CreateBookingHandler_MembersInjector;
import com.amazonaws.handler.DeleteBookingHandler;
import com.amazonaws.handler.DeleteBookingHandler_MembersInjector;
import com.amazonaws.handler.GetBookingHandler;
import com.amazonaws.handler.GetBookingHandler_MembersInjector;
import com.amazonaws.handler.GetBookingsHandler;
import com.amazonaws.handler.GetBookingsHandler_MembersInjector;
import com.amazonaws.handler.UpdateBookingHandler;
import com.amazonaws.handler.UpdateBookingHandler_MembersInjector;
import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.internal.DoubleCheck;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class DaggerBookingComponent implements BookingComponent {
  private Provider<ObjectMapper> objectMapperProvider;

  private Provider<DynamoDbClient> dynamoDbProvider;

  private Provider<String> tableNameProvider;

  private Provider<BookingDao> bookingDaoProvider;

  private DaggerBookingComponent(Builder builder) {
    initialize(builder);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static BookingComponent create() {
    return new Builder().build();
  }

  @SuppressWarnings("unchecked")
  private void initialize(final Builder builder) {
    this.objectMapperProvider =
        DoubleCheck.provider(BookingModule_ObjectMapperFactory.create(builder.bookingModule));
    this.dynamoDbProvider =
        DoubleCheck.provider(BookingModule_DynamoDbFactory.create(builder.bookingModule));
    this.tableNameProvider =
        DoubleCheck.provider(BookingModule_TableNameFactory.create(builder.bookingModule));
    this.bookingDaoProvider =
        DoubleCheck.provider(
            BookingModule_BookingDaoFactory.create(
                builder.bookingModule, dynamoDbProvider, tableNameProvider));
  }

  @Override
  public void inject(CreateBookingHandler requestHandler) {
    injectCreateBookingHandler(requestHandler);
  }

  @Override
  public void inject(DeleteBookingHandler requestHandler) {
    injectDeleteBookingHandler(requestHandler);
  }

  @Override
  public void inject(GetBookingHandler requestHandler) {
    injectGetBookingHandler(requestHandler);
  }

  @Override
  public void inject(GetBookingsHandler requestHandler) {
    injectGetBookingsHandler(requestHandler);
  }

  @Override
  public void inject(UpdateBookingHandler requestHandler) {
    injectUpdateBookingHandler(requestHandler);
  }

  private CreateBookingHandler injectCreateBookingHandler(CreateBookingHandler instance) {
    CreateBookingHandler_MembersInjector.injectObjectMapper(instance, objectMapperProvider.get());
    CreateBookingHandler_MembersInjector.injectBookingDao(instance, bookingDaoProvider.get());
    return instance;
  }

  private DeleteBookingHandler injectDeleteBookingHandler(DeleteBookingHandler instance) {
    DeleteBookingHandler_MembersInjector.injectObjectMapper(instance, objectMapperProvider.get());
    DeleteBookingHandler_MembersInjector.injectBookingDao(instance, bookingDaoProvider.get());
    return instance;
  }

  private GetBookingHandler injectGetBookingHandler(GetBookingHandler instance) {
    GetBookingHandler_MembersInjector.injectObjectMapper(instance, objectMapperProvider.get());
    GetBookingHandler_MembersInjector.injectBookingDao(instance, bookingDaoProvider.get());
    return instance;
  }

  private GetBookingsHandler injectGetBookingsHandler(GetBookingsHandler instance) {
    GetBookingsHandler_MembersInjector.injectObjectMapper(instance, objectMapperProvider.get());
    GetBookingsHandler_MembersInjector.injectBookingDao(instance, bookingDaoProvider.get());
    return instance;
  }

  private UpdateBookingHandler injectUpdateBookingHandler(UpdateBookingHandler instance) {
    UpdateBookingHandler_MembersInjector.injectObjectMapper(instance, objectMapperProvider.get());
    UpdateBookingHandler_MembersInjector.injectBookingDao(instance, bookingDaoProvider.get());
    return instance;
  }

  public static final class Builder {
    private BookingModule bookingModule;

    private Builder() {}

    public BookingComponent build() {
      if (bookingModule == null) {
        this.bookingModule = new BookingModule();
      }
      return new DaggerBookingComponent(this);
    }

    public Builder bookingModule(BookingModule bookingModule) {
      this.bookingModule = Preconditions.checkNotNull(bookingModule);
      return this;
    }
  }
}
