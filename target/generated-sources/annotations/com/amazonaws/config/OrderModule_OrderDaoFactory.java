package com.amazonaws.config;

import com.amazonaws.dao.OrderDao;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;
import javax.inject.Provider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class OrderModule_OrderDaoFactory implements Factory<OrderDao> {
  private final OrderModule module;

  private final Provider<DynamoDbClient> dynamoDbProvider;

  private final Provider<String> tableNameProvider;

  public OrderModule_OrderDaoFactory(
      OrderModule module,
      Provider<DynamoDbClient> dynamoDbProvider,
      Provider<String> tableNameProvider) {
    this.module = module;
    this.dynamoDbProvider = dynamoDbProvider;
    this.tableNameProvider = tableNameProvider;
  }

  @Override
  public OrderDao get() {
    return provideInstance(module, dynamoDbProvider, tableNameProvider);
  }

  public static OrderDao provideInstance(
      OrderModule module,
      Provider<DynamoDbClient> dynamoDbProvider,
      Provider<String> tableNameProvider) {
    return proxyOrderDao(module, dynamoDbProvider.get(), tableNameProvider.get());
  }

  public static OrderModule_OrderDaoFactory create(
      OrderModule module,
      Provider<DynamoDbClient> dynamoDbProvider,
      Provider<String> tableNameProvider) {
    return new OrderModule_OrderDaoFactory(module, dynamoDbProvider, tableNameProvider);
  }

  public static OrderDao proxyOrderDao(
      OrderModule instance, DynamoDbClient dynamoDb, String tableName) {
    return Preconditions.checkNotNull(
        instance.orderDao(dynamoDb, tableName),
        "Cannot return null from a non-@Nullable @Provides method");
  }
}
