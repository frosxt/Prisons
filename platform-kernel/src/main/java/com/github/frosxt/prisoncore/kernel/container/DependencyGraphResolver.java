package com.github.frosxt.prisoncore.kernel.container;

import com.github.frosxt.prisoncore.api.service.ServiceDescriptor;
import com.github.frosxt.prisoncore.api.service.ServiceException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class DependencyGraphResolver {

    public static <T> List<ServiceDescriptor<?>> resolve(final Collection<ServiceBinding<?>> bindings) {
        final Map<Class<?>, ServiceDescriptor<?>> descriptorMap = new HashMap<>();
        final Map<Class<?>, Set<Class<?>>> adjacency = new HashMap<>();
        final Map<Class<?>, Integer> inDegree = new HashMap<>();

        for (final ServiceBinding<?> binding : bindings) {
            final ServiceDescriptor<?> desc = binding.descriptor();
            descriptorMap.put(desc.type(), desc);
            adjacency.putIfAbsent(desc.type(), new HashSet<>());
            inDegree.putIfAbsent(desc.type(), 0);
        }

        for (final ServiceBinding<?> binding : bindings) {
            final ServiceDescriptor<?> desc = binding.descriptor();
            for (final Class<?> dep : desc.dependencies()) {
                if (descriptorMap.containsKey(dep)) {
                    adjacency.computeIfAbsent(dep, k -> new HashSet<>()).add(desc.type());
                    inDegree.merge(desc.type(), 1, Integer::sum);
                }
            }
        }

        final Queue<Class<?>> queue = new LinkedList<>();
        for (final Map.Entry<Class<?>, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        final List<ServiceDescriptor<?>> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            final Class<?> current = queue.poll();
            final ServiceDescriptor<?> desc = descriptorMap.get(current);
            if (desc != null) {
                sorted.add(desc);
            }
            for (final Class<?> neighbor : adjacency.getOrDefault(current, Collections.emptySet())) {
                final int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (sorted.size() < descriptorMap.size()) {
            final Set<Class<?>> remaining = new HashSet<>(descriptorMap.keySet());
            sorted.forEach(d -> remaining.remove(d.type()));
            throw new ServiceException("Circular dependency detected among services: " + remaining);
        }

        return sorted;
    }
}
