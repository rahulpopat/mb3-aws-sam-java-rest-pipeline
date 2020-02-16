package com.amazonaws.config;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class OrderModule_DynamoDbFactory implements Factory<DynamoDbClient> {
  private final OrderModule module;

  public OrderModule_DynamoDbFactory(OrderModule module) {
    this.module = module;
  }

  @Override
  public DynamoDbClient get() {
    return provideInstance(module);
  }

  public static DynamoDbClient provideInstance(OrderModule module) {
    return proxyDynamoDb(module);
  }

  public static OrderModule_DynamoDbFactory create(OrderModule module) {
    return new OrderModule_DynamoDbFactory(module);
  }

  public static DynamoDbClient proxyDynamoDb(OrderModule instance) {
    return Preconditions.checkNotNull(
        instance.dynamoDb(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
