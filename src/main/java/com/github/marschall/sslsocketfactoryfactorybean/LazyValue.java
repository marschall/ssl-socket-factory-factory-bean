package com.github.marschall.sslsocketfactoryfactorybean;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Supplier;

/**
 * A lazily computed value.
 * <p>
 * Instances of this class are thread-safe.
 *
 * @param <T> the type of results supplied by this supplier
 */
final class LazyValue<T> implements Supplier<T> {

  private final Supplier<T> delegate;

  /*
   * As the value needs to be computed only once we chose a ReentrantReadWriteLock over a synchronized.
   * This allows concurrent access without blocking to the computed value.
   * This has the advantage over a CAS that the value is computed only once.
   */
  private final ReentrantReadWriteLock lock;
  private T value;

  /**
   * Constructs a new {@link LazyValue}.
   *
   * @param delegate the supplier to supply the actual value,
   *        must not be {@code null},
   *        must not return {@code null}
   */
  LazyValue(Supplier<T> delegate) {
    Objects.requireNonNull(delegate);
    this.delegate = delegate;
    this.lock = new ReentrantReadWriteLock();
  }

  @Override
  public T get() {
    // common case, value is computed
    ReadLock readLock = this.lock.readLock();
    readLock.lock();
    try {
      if (this.value != null) {
        return this.value;
      }
    } finally {
      readLock.unlock();
    }

    // uncommon case, value needs to be computed
    WriteLock writeLock = this.lock.writeLock();
    try {
      if (this.value != null) {
        return this.value;
      }
      T newValue = this.delegate.get();
      Objects.requireNonNull(newValue, "value");
      this.value = newValue;
      return newValue;
    } finally {
      writeLock.unlock();
    }
  }



}
