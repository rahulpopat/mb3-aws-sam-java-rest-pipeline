package com.amazonaws.config;

import com.amazonaws.dao.BookingDao;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class BookingModule_BookingDaoFactory implements Factory<BookingDao> {
  private final BookingModule module;

  private final Provider<DynamoDbClient> dynamoDbProvider;

  private final Provider<String> tableNameProvider;

  public BookingModule_BookingDaoFactory(
      BookingModule module,
      Provider<DynamoDbClient> dynamoDbProvider,
      Provider<String> tableNameProvider) {
    this.module = module;
    this.dynamoDbProvider = dynamoDbProvider;
    this.tableNameProvider = tableNameProvider;
  }

  @Override
  public BookingDao get() {
    return provideInstance(module, dynamoDbProvider, tableNameProvider);
  }

  public static BookingDao provideInstance(
      BookingModule module,
      Provider<DynamoDbClient> dynamoDbProvider,
      Provider<String> tableNameProvider) {
    return proxyBookingDao(module, dynamoDbProvider.get(), tableNameProvider.get());
  }

  public static BookingModule_BookingDaoFactory create(
      BookingModule module,
      Provider<DynamoDbClient> dynamoDbProvider,
      Provider<String> tableNameProvider) {
    return new BookingModule_BookingDaoFactory(module, dynamoDbProvider, tableNameProvider);
  }

  public static BookingDao proxyBookingDao(
      BookingModule instance, DynamoDbClient dynamoDb, String tableName) {
    return Preconditions.checkNotNull(
        instance.bookingDao(dynamoDb, tableName),
        "Cannot return null from a non-@Nullable @Provides method");
  }
}
