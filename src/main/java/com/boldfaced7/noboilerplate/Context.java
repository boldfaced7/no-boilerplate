package com.boldfaced7.noboilerplate;

import java.util.function.Consumer;
import java.util.function.Function;

public class Context<M, A> {
    private final Mocker<M> mocker = new Mocker<>();
    private final Asserter<A> asserter = new Asserter<>();
    private final String message;

    public Context(String message) {
        this.message = message;
    }

    public <N> void willDo(Function<M, N> mocked, Consumer<N> action) {
        mocker.mock(mocked, action);
    }
    public <N, O> void willDo(Function<M, N> mocked, Function<N, O> action, O result) {
        mocker.mock(mocked, action, result);
    }
    public <N> void willThrow(Function<M, N> mocked, Consumer<N> action, Class<? extends Throwable> toBeThrown) {
        mocker.mockThrowable(mocked, action, toBeThrown);
    }

    public void given(M mocked) {
        mocker.given(mocked);
    }
    public void then(M mocked) {
        mocker.then(mocked);
    }

    public void willBe(Runnable assertion) {
        asserter.add(assertion);
    }
    public void willBe(Consumer<A> assertion) {
        asserter.add(assertion);
    }
    public void willCatch(Consumer<Throwable> assertion) {
        asserter.addThrowable(assertion);
    }

    public void doAssert() {
        asserter.execute();
    }
    public void doAssert(A target) {
        asserter.execute(target);
    }
    public void doAssert(Throwable throwable) {
        asserter.execute(throwable);
    }

    @Override
    public String toString() {
        return message;
    }
}