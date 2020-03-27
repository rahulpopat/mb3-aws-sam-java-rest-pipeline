package com.amazonaws.config;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class BookingModule_DynamoDbFactory implements Factory<DynamoDbClient> {
  private final BookingModule module;

  public BookingModule_DynamoDbFactory(BookingModule module) {
    this.module = module;
  }

  @Override
  public DynamoDbClient get() {
    return provideInstance(module);
  }

  public static DynamoDbClient provideInstance(BookingModule module) {
    return proxyDynamoDb(module);
  }

  public static BookingModule_DynamoDbFactory create(BookingModule module) {
    return new BookingModule_DynamoDbFactory(module);
  }

  public static DynamoDbClient proxyDynamoDb(BookingModule instance) {
    return Preconditions.checkNotNull(
        instance.dynamoDb(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
