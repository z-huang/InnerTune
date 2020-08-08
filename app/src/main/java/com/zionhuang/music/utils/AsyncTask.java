package com.zionhuang.music.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncTask<Argument, Result> {

    private static ExecutorService sExecutor = Executors.newCachedThreadPool();
    private static Handler sHandler = new Handler(Looper.getMainLooper());

    private AtomicBoolean mIsCancelled = new AtomicBoolean();
    private AtomicBoolean mIsOngoing = new AtomicBoolean();

    private Function<Argument, Result> mDoInBackgroundFn;
    private Consumer<Result> mOnDoneFn;
    private Consumer<Exception> mOnErrorFn;

    /**
     * If invoked on main thread, guarantees that onWorkDone and onError will never be called
     */
    public final void cancel() {
        mIsCancelled.set(true);
    }

    public final boolean isCancelled() {
        return mIsCancelled.get();
    }

    public final boolean isOngoing() {
        return mIsOngoing.get();
    }

    public final AsyncTask<Argument, Result> execute(Argument args) {
        if (isOngoing()) {
            throw new IllegalStateException("Unable to execute a task that is already ongoing");
        }
        if (mDoInBackgroundFn == null) {
            throw new IllegalStateException("Unable to execute a task that has no work assigned");
        }

        mIsOngoing.set(true);
        sExecutor.submit(() -> {
            try {
                Result result = mDoInBackgroundFn.apply(args);
                sHandler.post(() -> {
                    if (isCancelled()) {
                        mIsOngoing.set(false);
                        return;
                    }

                    if (mOnDoneFn != null) {
                        mOnDoneFn.accept(result);
                    }
                    mIsOngoing.set(false);
                });
            } catch (Exception e) {
                sHandler.post(() -> {
                    if (isCancelled()) {
                        mIsOngoing.set(false);
                        return;
                    }

                    if (mOnErrorFn != null) {
                        mOnErrorFn.accept(e);
                    }
                    mIsOngoing.set(false);
                });
            }
        });

        return this;
    }

    public AsyncTask<Argument, Result> doInBackground(Function<Argument, Result> fn) {
        mDoInBackgroundFn = fn;
        return this;
    }

    public AsyncTask<Argument, Result> onDone(Consumer<Result> fn) {
        mOnDoneFn = fn;
        return this;
    }

    public AsyncTask<Argument, Result> onError(Consumer<Exception> fn) {
        mOnErrorFn = fn;
        return this;
    }
}