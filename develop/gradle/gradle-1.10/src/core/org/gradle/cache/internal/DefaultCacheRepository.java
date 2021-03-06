/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.cache.internal;

import org.gradle.CacheUsage;
import org.gradle.api.Action;
import org.gradle.cache.*;
import org.gradle.cache.internal.filelock.LockOptions;
import org.gradle.messaging.serialize.DefaultSerializer;
import org.gradle.messaging.serialize.Serializer;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.gradle.cache.internal.FileLockManager.LockMode;
import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;

public class DefaultCacheRepository implements CacheRepository {
    private final File globalCacheDir;
    private final CacheUsage cacheUsage;
    private final File projectCacheDir;
    private final CacheFactory factory;

    public DefaultCacheRepository(File userHomeDir, File projectCacheDir, CacheUsage cacheUsage, CacheFactory factory) {
        this.projectCacheDir = projectCacheDir;
        this.factory = factory;
        this.globalCacheDir = new File(userHomeDir, "caches");
        this.cacheUsage = cacheUsage;
    }

    public DirectoryCacheBuilder store(String key) {
        return new PersistentStoreBuilder(key);
    }

    public DirectoryCacheBuilder cache(String key) {
        return new PersistentCacheBuilder(key);
    }

    public <E> ObjectCacheBuilder<E, PersistentStateCache<E>> stateCache(Class<E> elementType, String key) {
        return new StateCacheBuilder<E>(key);
    }

    public <K, V> ObjectCacheBuilder<V, PersistentIndexedCache<K, V>> indexedCache(Class<K> keyType, Class<V> elementType, String key) {
        return new IndexedCacheBuilder<K, V>(key);
    }

    private abstract class AbstractCacheBuilder<T> implements CacheBuilder<T> {
        private final String key;
        private Map<String, ?> properties = Collections.emptyMap();
        private CacheLayout layout;
        private CacheValidator validator;

        protected AbstractCacheBuilder(String key) {
            this.key = key;
            this.layout = new CacheLayoutBuilder().build();
        }

        public CacheBuilder<T> withProperties(Map<String, ?> properties) {
            this.properties = properties;
            return this;
        }

        public CacheBuilder<T> withLayout(CacheLayout layout) {
            this.layout = layout;
            return this;
        }

        public CacheBuilder<T> withValidator(CacheValidator validator) {
            this.validator = validator;
            return this;
        }

        public T open() {
            File cacheBaseDir = layout.getCacheDir(globalCacheDir, projectCacheDir, key);
            Map<String, ?> props = layout.applyLayoutProperties(properties);
            return doOpen(cacheBaseDir, props, validator);
        }

        protected abstract T doOpen(File cacheDir, Map<String, ?> properties, CacheValidator validator);

    }

    private class PersistentCacheBuilder extends AbstractCacheBuilder<PersistentCache> implements DirectoryCacheBuilder {
        Action<? super PersistentCache> initializer;
        LockOptions lockOptions = mode(LockMode.Shared);
        String displayName;

        protected PersistentCacheBuilder(String key) {
            super(key);
        }

        @Override
        public DirectoryCacheBuilder withLayout(CacheLayout layout) {
            super.withLayout(layout);
            return this;
        }

        @Override
        public DirectoryCacheBuilder withProperties(Map<String, ?> properties) {
            super.withProperties(properties);
            return this;
        }

        @Override
        public DirectoryCacheBuilder withValidator(CacheValidator validator) {
            super.withValidator(validator);
            return this;
        }

        public DirectoryCacheBuilder withInitializer(Action<? super PersistentCache> initializer) {
            this.initializer = initializer;
            return this;
        }

        public DirectoryCacheBuilder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public DirectoryCacheBuilder withLockOptions(LockOptions lockOptions) {
            this.lockOptions = lockOptions;
            return this;
        }

        @Override
        protected PersistentCache doOpen(File cacheDir, Map<String, ?> properties, CacheValidator validator) {
            return factory.open(cacheDir, displayName, cacheUsage, validator, properties, lockOptions, initializer);
        }
    }

    private class PersistentStoreBuilder extends PersistentCacheBuilder {
        private PersistentStoreBuilder(String key) {
            super(key);
        }

        @Override
        protected PersistentCache doOpen(File cacheDir, Map<String, ?> properties, CacheValidator validator) {
            if (!properties.isEmpty()) {
                throw new UnsupportedOperationException("Properties are not supported for stores.");
            }
            return factory.openStore(cacheDir, displayName, lockOptions, initializer);
        }
    }

    private abstract class AbstractObjectCacheBuilder<E, T> extends AbstractCacheBuilder<T> implements ObjectCacheBuilder<E, T> {
        protected Serializer<E> serializer = new DefaultSerializer<E>();

        protected AbstractObjectCacheBuilder(String key) {
            super(key);
        }

        @Override
        public ObjectCacheBuilder<E, T> withProperties(Map<String, ?> properties) {
            super.withProperties(properties);
            return this;
        }

        @Override
        public ObjectCacheBuilder<E, T> withLayout(CacheLayout layout) {
            super.withLayout(layout);
            return this;
        }

        public ObjectCacheBuilder<E, T> withSerializer(Serializer<E> serializer) {
            this.serializer = serializer;
            return this;
        }
    }

    private class StateCacheBuilder<E> extends AbstractObjectCacheBuilder<E, PersistentStateCache<E>>  {
        protected StateCacheBuilder(String key) {
            super(key);
        }

        @Override
        protected PersistentStateCache<E> doOpen(File cacheDir, Map<String, ?> properties, CacheValidator validator) {
            return factory.openStateCache(cacheDir, cacheUsage, validator, properties, mode(LockMode.Exclusive), serializer);
        }
    }

    private class IndexedCacheBuilder<K, V> extends AbstractObjectCacheBuilder<V, PersistentIndexedCache<K, V>> {
        private IndexedCacheBuilder(String key) {
            super(key);
        }

        @Override
        protected PersistentIndexedCache<K, V> doOpen(File cacheDir, Map<String, ?> properties, CacheValidator validator) {
            return factory.openIndexedCache(cacheDir, cacheUsage, validator, properties, mode(LockMode.Exclusive), serializer);
        }
    }
}
