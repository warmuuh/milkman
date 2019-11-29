package me.dinowernli.grpc.polyglot.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

import io.grpc.stub.StreamObserver;

/**
 * A {@link StreamObserver} which groups multiple observers and executes them all.
 */
public class CompositeStreamObserver<T> implements StreamObserver<T> {
  private static final Logger logger = LoggerFactory.getLogger(CompositeStreamObserver.class);
  private final ImmutableList<StreamObserver<T>> observers;

  @SafeVarargs
  public static <T> CompositeStreamObserver<T> of(StreamObserver<T>... observers) {
    return new CompositeStreamObserver<T>(ImmutableList.copyOf(observers));
  }

  private CompositeStreamObserver(ImmutableList<StreamObserver<T>> observers) {
    this.observers = observers;
  }

  @Override
  public void onCompleted() {
    for (StreamObserver<T> observer : observers) {
      try {
        observer.onCompleted();
      } catch (Throwable t) {
        logger.error("Exception in composite onComplete, moving on", t);
      }
    }
  }

  @Override
  public void onError(Throwable t) {
    for (StreamObserver<T> observer : observers) {
      try {
        observer.onError(t);
      } catch (Throwable s) {
        logger.error("Exception in composite onError, moving on", s);
      }
    }
  }

  @Override
  public void onNext(T value) {
    for (StreamObserver<T> observer : observers) {
      try {
        observer.onNext(value);
      } catch (Throwable t) {
        logger.error("Exception in composite onNext, moving on", t);
      }
    }
  }
}