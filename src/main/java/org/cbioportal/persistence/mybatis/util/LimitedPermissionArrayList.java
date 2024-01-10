package org.cbioportal.persistence.mybatis.util;

import java.util.*;
import java.util.function.*;

public class LimitedPermissionArrayList<T> extends ArrayList<T> {

    public boolean addAll(Collection<? extends T> c) {
        System.out.println("addAll called");
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection<? extends T> c) {
        System.out.println("addAll called");
        throw new UnsupportedOperationException();
    }

    public void clear() {
        System.out.println("clear called");
        throw new UnsupportedOperationException();
    }

    public T remove(int index) {
        System.out.println("remove called");
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        System.out.println("LimitedPermissionArrayList:remove() called, throwing UnsupportedOperationException()");
        throw new UnsupportedOperationException();
    }
    
    public boolean removeAll(Collection<?> c) {
        System.out.println("LimitedPermissionArrayList:removeAll() called, throwing UnsupportedOperationException()");
        throw new UnsupportedOperationException();
    }

    public boolean removeIf(Predicate<? super T> filter) {
        System.out.println("LimitedPermissionArrayList:removeIF() called, throwing UnsupportedOperationException()");
        throw new UnsupportedOperationException();
    }

    protected void removeRange(int fromIndex, int toIndex) {
        System.out.println("LimitedPermissionArrayList:removeRange() called, throwing UnsupportedOperationException()");
        throw new UnsupportedOperationException();
    }

    public void replaceAll(UnaryOperator<T> operator) {
        System.out.println("LimitedPermissionArrayList:replaceAll() called, throwing UnsupportedOperationException()");
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        System.out.println("LimitedPermissionArrayList:retainAlll() called, throwing UnsupportedOperationException()");
        throw new UnsupportedOperationException();
    }

    public T set(int index, T element) {
        System.out.println("LimitedPermissionArrayList:set() called, throwing UnsupportedOperationException()");
        throw new UnsupportedOperationException();
    }
}
