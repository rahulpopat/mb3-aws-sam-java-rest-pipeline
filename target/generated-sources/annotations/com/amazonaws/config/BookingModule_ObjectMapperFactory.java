package com.amazonaws.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class BookingModule_ObjectMapperFactory implements Factory<ObjectMapper> {
  private final BookingModule module;

  public BookingModule_ObjectMapperFactory(BookingModule module) {
    this.module = module;
  }

  @Override
  public ObjectMapper get() {
    return provideInstance(module);
  }

  public static ObjectMapper provideInstance(BookingModule module) {
    return proxyObjectMapper(module);
  }

  public static BookingModule_ObjectMapperFactory create(BookingModule module) {
    return new BookingModule_ObjectMapperFactory(module);
  }

  public static ObjectMapper proxyObjectMapper(BookingModule instance) {
    return Preconditions.checkNotNull(
        instance.objectMapper(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
