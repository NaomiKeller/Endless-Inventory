package com.kwwsyk.endinv.common.options;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Deprecated
public interface LegacyIConfigValue<T> {
    T get();

    void set(T t);

    static <T> LegacyIConfigValue<T> of(Supplier<T> getter, Consumer<T> setter){
        return new LegacyIConfigValue<T>() {
            @Override
            public T get() {
                return getter.get();
            }

            @Override
            public void set(T t) {
                setter.accept(t);
            }
        };
    }

    static <T> LegacyIConfigValue<T> of(Supplier<T> getter, Consumer<T> setter, Runnable onset){
        return new LegacyIConfigValue<T>() {
            @Override
            public T get() {
                return getter.get();
            }

            @Override
            public void set(T t) {
                setter.accept(t);
                onset.run();
            }
        };
    }
}
