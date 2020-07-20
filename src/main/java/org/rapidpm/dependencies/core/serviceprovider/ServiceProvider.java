/**
 * Copyright © 2013 Sven Ruppert (sven.ruppert@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rapidpm.dependencies.core.serviceprovider;

import org.rapidpm.dependencies.core.logger.HasLogger;
import org.rapidpm.dependencies.core.logger.Logger;
import org.rapidpm.frp.model.Result;

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

public interface ServiceProvider<T>
    extends HasLogger {

  default boolean failWithException() {
    return false;
  }

  default Function<Class<T>, Result<? extends T>> loadServiceFkt() {
    return (service) -> {
      Iterator<T> iterator = ServiceLoader.load(service)
                                          .iterator();
      Iterable<T>  iterable = () -> iterator;
      final Set<T> set      = stream(iterable.spliterator(), false).collect(toSet());
      if (set.isEmpty()) {
        final String msg = "no implementation found for interface " + service.getName();
        Logger.getLogger(service)
              .warning(msg);
        if (failWithException()) throw new RuntimeException("no implementation found for interface " + service.getName());
        return Result.failure(msg);
      }

      if (set.size() > 1) {
        final String msg = "to many implementations found for interface " + service.getName();
        Logger.getLogger(service)
              .warning(msg);
        if (failWithException()) throw new RuntimeException("to many implementations found for interface " + service.getName());
        return Result.failure(msg);
      }
      return Result.success(set.iterator()
                               .next());
    };
  }

  Class<T> serviceInterface();

  default Result<? extends T> load() {
    return loadServiceFkt().apply(serviceInterface());
  }
}
