package org.flinkcoin.helper;

import java.util.function.Consumer;

@FunctionalInterface
public interface ThrowableConsumer<T> extends Consumer<T> {

    @Override
    default void accept(final T elem) {
        try {
            acceptThrowsException(elem);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    void acceptThrowsException(T elem) throws Exception;
}
