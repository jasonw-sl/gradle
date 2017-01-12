/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.api.internal.changedetection.state;

import com.google.common.collect.Lists;
import com.google.common.hash.HashCode;
import org.gradle.api.Task;
import org.gradle.api.internal.tasks.execution.TaskCacheHashesListener;
import org.gradle.caching.BuildCacheKey;
import org.gradle.caching.internal.BuildCacheKeyBuilder;
import org.gradle.caching.internal.DefaultBuildCacheKeyBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskCacheKeyCalculator {
    private final TaskCacheHashesListener cacheHashesListener;

    public TaskCacheKeyCalculator(TaskCacheHashesListener cacheHashesListener) {
        this.cacheHashesListener = cacheHashesListener;
    }

    public BuildCacheKey calculate(TaskExecution execution, Task task) {
        if (execution.getTaskActionsClassLoaderHash() == null || execution.getTaskActionsClassLoaderHash() == null) {
            cacheHashesListener.taskCacheKey(null, task);
            return null;
        }

        BuildCacheKeyBuilder builder = new DefaultBuildCacheKeyBuilder();
        builder.putString(execution.getTaskClass());
        byte[] taskClassloaderHash = execution.getTaskClassLoaderHash().asBytes();
        cacheHashesListener.taskClassloaderHash(taskClassloaderHash, task);
        builder.putBytes(taskClassloaderHash);
        byte[] taskActionsClassloaderHash = execution.getTaskActionsClassLoaderHash().asBytes();
        cacheHashesListener.taskActionsClassloaderHash(taskActionsClassloaderHash, task);
        builder.putBytes(taskActionsClassloaderHash);

        // TODO:LPTR Use sorted maps instead of explicitly sorting entries here

        for (Map.Entry<String, Object> entry : sortEntries(execution.getInputProperties().entrySet())) {
            String propertyName = entry.getKey();
            builder.putString(propertyName);
            Object value = entry.getValue();
            byte[] hash = hashForObject(value).asBytes();
            cacheHashesListener.propertyHash(propertyName, hash, task);
            builder.putBytes(hash);
            builder.appendToCacheKey(value);
        }

        for (Map.Entry<String, FileCollectionSnapshot> entry : sortEntries(execution.getInputFilesSnapshot().entrySet())) {
            String propertyName = entry.getKey();
            builder.putString(propertyName);
            FileCollectionSnapshot snapshot = entry.getValue();
            DefaultBuildCacheKeyBuilder newBuilder = new DefaultBuildCacheKeyBuilder();
            snapshot.appendToCacheKey(newBuilder);
            byte[] hash = newBuilder.buildHashCode().asBytes();
            cacheHashesListener.propertyHash(propertyName, hash, task);
            builder.putBytes(hash);
        }

        for (String cacheableOutputPropertyName : sortStrings(execution.getOutputPropertyNamesForCacheKey())) {
            cacheHashesListener.outputPropertyName(cacheableOutputPropertyName, task);
            builder.putString(cacheableOutputPropertyName);
        }

        BuildCacheKey cacheKey = builder.build();
        cacheHashesListener.taskCacheKey(cacheKey, task);
        return cacheKey;
    }

    private static <T> List<Map.Entry<String, T>> sortEntries(Set<Map.Entry<String, T>> entries) {
        List<Map.Entry<String, T>> sortedEntries = Lists.newArrayList(entries);
        Collections.sort(sortedEntries, new Comparator<Map.Entry<String, T>>() {
            @Override
            public int compare(Map.Entry<String, T> o1, Map.Entry<String, T> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });
        return sortedEntries;
    }

    private static List<String> sortStrings(Collection<String> entries) {
        List<String> sortedEntries = Lists.newArrayList(entries);
        Collections.sort(sortedEntries);
        return sortedEntries;
    }

    private static HashCode hashForObject(Object object) {
        return ((DefaultBuildCacheKeyBuilder) new DefaultBuildCacheKeyBuilder().appendToCacheKey(object)).buildHashCode();
    }
}
