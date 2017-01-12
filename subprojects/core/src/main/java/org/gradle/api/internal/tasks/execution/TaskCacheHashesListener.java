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

package org.gradle.api.internal.tasks.execution;

import org.gradle.api.Task;
import org.gradle.caching.BuildCacheKey;

public interface TaskCacheHashesListener {
    class Empty implements TaskCacheHashesListener {
        @Override
        public void taskCacheKey(BuildCacheKey key, Task task) {
        }

        @Override
        public void propertyHash(String propertyName, byte[] hash, Task task) {
        }

        @Override
        public void taskClassloaderHash(byte[] hash, Task task) {
        }

        @Override
        public void taskActionsClassloaderHash(byte[] hash, Task task) {
        }

        @Override
        public void outputPropertyName(String name, Task task) {
        }
    }

    void taskCacheKey(BuildCacheKey key, Task task);

    void propertyHash(String propertyName, byte[] hash, Task task);

    void taskClassloaderHash(byte[] hash, Task task);

    void taskActionsClassloaderHash(byte[] hash, Task task);

    void outputPropertyName(String name, Task task);
}
