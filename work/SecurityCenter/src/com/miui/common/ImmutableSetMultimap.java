
package com.miui.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class ImmutableSetMultimap<S, T> {

    private HashMap<S, HashSet<T>> mData = new HashMap<S, HashSet<T>>();

    public static class Builder<U, V> {

        private ImmutableSetMultimap<U, V> mInst = new ImmutableSetMultimap<U, V>();

        public Builder<U, V> put(U key, V value) {
            if (!mInst.mData.containsKey(key)) {
                mInst.mData.put(key, new HashSet<V>());
            }
            mInst.mData.get(key).add(value);
            return this;
        }

        public ImmutableSetMultimap<U, V> build() {
            return mInst;
        }

    }

    private ImmutableSetMultimap() {
    }

    public Set<T> get(S key) {
        return mData.get(key);
    }

}
