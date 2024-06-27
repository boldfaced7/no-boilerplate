package com.boldfaced7.noboilerplate;

import java.util.function.Supplier;

public class TestTemplate {

    public static <T> void doTest(Runnable toBeTested, Context<T, ?> context, T mocked) {
        perform(context, mocked, toBeTested).run();
    }

    public static <T, R> void doTest(Supplier<R> toBeTested, Context<T, R> context, T mocked) {
        perform(context, mocked, () -> context.doAssert(toBeTested.get())).run();
    }

    private static <T, R> Runnable perform(Context<T, R> context, T mocked, Runnable runnable) {
        return () -> {
            context.given(mocked);
            try {
                runnable.run();
                context.doAssert();
            } catch (Throwable t) {
                context.doAssert(t);
            }
            context.then(mocked);
        };
    }
}
