package com.boldfaced7.noboilerplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Asserter<T> {
    private final List<Runnable> runnables = new ArrayList<>();
    private final List<Consumer<T>> consumers = new ArrayList<>();
    private final List<Consumer<Throwable>> throwables = new ArrayList<>();
    private String message;

    public Asserter() {}
    public Asserter(String message) {
        this.message = message;
    }

    public void add(Runnable assertion) {
        runnables.add(assertion);
    }
    public void add(Consumer<T> assertion) {
        consumers.add(assertion);
    }
    public void addThrowable(Consumer<Throwable> assertion) {
        throwables.add(assertion);
    }

    public void execute() {
        runnables.forEach(Runnable::run);
    }
    public void execute(T target) {
        consumers.forEach(a -> a.accept(target));
    }
    public void execute(Throwable t) {
        throwables.forEach(a -> a.accept(t));
    }

    @Override
    public String toString() {
        return message;
    }
}