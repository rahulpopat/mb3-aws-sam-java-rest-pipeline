package com.amazonaws.config;

import dagger.internal.Factory;
import dagger.internal.Preconditions;
import javax.annotation.Generated;

@Generated(
  value = "dagger.internal.codegen.ComponentProcessor",
  comments = "https://google.github.io/dagger"
)
public final class BookingModule_TableNameFactory implements Factory<String> {
  private final BookingModule module;

  public BookingModule_TableNameFactory(BookingModule module) {
    this.module = module;
  }

  @Override
  public String get() {
    return provideInstance(module);
  }

  public static String provideInstance(BookingModule module) {
    return proxyTableName(module);
  }

  public static BookingModule_TableNameFactory create(BookingModule module) {
    return new BookingModule_TableNameFactory(module);
  }

  public static String proxyTableName(BookingModule instance) {
    return Preconditions.checkNotNull(
        instance.tableName(), "Cannot return null from a non-@Nullable @Provides method");
  }
}
