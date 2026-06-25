package org.cbioportal.legacy.persistence.mybatis.util;

import java.util.*;
import java.util.function.*;

public class LimitedPermissionArrayList<T> extends ArrayList<T> {

  public boolean addAll(Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  public boolean addAll(int index, Collection<? extends T> c) {
    throw new UnsupportedOperationException();
  }

  public void clear() {
    throw new UnsupportedOperationException();
  }

  public T remove(int index) {
    throw new UnsupportedOperationException();
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  public boolean removeIf(Predicate<? super T> filter) {
    throw new UnsupportedOperationException();
  }

  protected void removeRange(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException();
  }

  public void replaceAll(UnaryOperator<T> operator) {
    throw new UnsupportedOperationException();
  }

  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  public T set(int index, T element) {
    throw new UnsupportedOperationException();
  }
}
