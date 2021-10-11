package org.flinkcoin.helper;

import java.util.function.Function;

@FunctionalInterface
public interface ThrowableFunction<T, U> extends Function<T, U> {

    @Override
    default U apply(final T elem) {
        try {
            return applyThrowsException(elem);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    U applyThrowsException(T elem) throws Exception;
}
