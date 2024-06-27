package com.boldfaced7.noboilerplate;

import org.mockito.BDDMockito;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Mocker<T> {

    private final List<Consumer<T>> givens = new ArrayList<>();
    private final List<Consumer<T>> thens = new ArrayList<>();
    private String message;

    public Mocker() {}
    public Mocker(String message) {
        this.message = message;
    }

    public <U> void mock(Function<T, U> mocked, Consumer<U> action) {
        Consumer<T> given = t -> action.accept(BDDMockito.willDoNothing().given(mocked.apply(t)));
        Consumer<T> then  = t -> action.accept(BDDMockito.then(mocked.apply(t)).should());
        add(given, then);
    }
    public <U, R> void mock(Function<T, U> mocked, Function<U, R> action, R result) {
        Consumer<T> given = t -> BDDMockito.given(action.apply(mocked.apply(t))).willReturn(result);
        Consumer<T> then  = t -> action.apply(BDDMockito.then(mocked.apply(t)).should());
        add(given, then);
    }
    public <U> void mockThrowable(Function<T, U> mocked, Consumer<U> action, Class<? extends Throwable> toBeThrown) {
        Consumer<T> given = t -> action.accept(BDDMockito.willThrow(toBeThrown).given(mocked.apply(t)));
        Consumer<T> then  = t -> action.accept(BDDMockito.then(mocked.apply(t)).should());
        add(given, then);
    }

    public void given(T t) {
        givens.forEach(given -> given.accept(t));
    }
    public void then(T t) {
        thens.forEach(then -> then.accept(t));
    }

    public void add(Consumer<T> given, Consumer<T> then) {
        givens.add(given);
        thens.add(then);
    }

    @Override
    public String toString() {
        return message;
    }

}