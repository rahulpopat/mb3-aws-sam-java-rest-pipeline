package com.amazonaws.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class OrderModule_ObjectMapperFactory implements Factory<ObjectMapper> {
  private final OrderModule module;

  public OrderModule_ObjectMapperFactory(OrderModule module) {
    this.module = module;
  }

  @Override
  public ObjectMapper get() {
    return provideInstance(module);
  }

  public static ObjectMapper provideInstance(OrderModule module) {
    return proxyObjectMapper(module);
  }

  public static OrderModule_ObjectMapperFactory create(OrderModule module) {
    return new OrderModule_ObjectMapperFactory(module);
  }

  public static ObjectMapper proxyObjectMapper(OrderModule instance) {
    return Preconditions.checkNotNull(
        instance.objectMapper(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
