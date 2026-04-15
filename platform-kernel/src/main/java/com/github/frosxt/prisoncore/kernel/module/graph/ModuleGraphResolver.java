package com.github.frosxt.prisoncore.kernel.module.graph;

import com.github.frosxt.prisoncore.api.module.ModuleDescriptor;
import com.github.frosxt.prisoncore.api.service.ServiceException;
import com.github.frosxt.prisoncore.spi.module.ModuleCandidate;

import java.util.*;

public final class ModuleGraphResolver {

    public static List<ModuleCandidate> resolve(List<ModuleCandidate> candidates) {
        final Map<String, ModuleCandidate> byId = new LinkedHashMap<>();
        final Map<String, Set<String>> capabilityProviders = new HashMap<>();

        for (final ModuleCandidate candidate : candidates) {
            final ModuleDescriptor desc = candidate.descriptor();
            byId.put(desc.id(), candidate);
            for (final String cap : desc.providesCapabilities()) {
                capabilityProviders.computeIfAbsent(cap, k -> new HashSet<>()).add(desc.id());
            }
        }

        final Set<String> rejected = new HashSet<>();
        for (final ModuleCandidate candidate : candidates) {
            final ModuleDescriptor desc = candidate.descriptor();
            for (final String dep : desc.requiredDependencies()) {
                if (!byId.containsKey(dep)) {
                    rejected.add(desc.id());
                    break;
                }
            }
            if (!rejected.contains(desc.id())) {
                for (final String cap : desc.requiresCapabilities()) {
                    if (!capabilityProviders.containsKey(cap)) {
                        rejected.add(desc.id());
                        break;
                    }
                }
            }
        }

        if (!rejected.isEmpty()) {
            boolean changed = true;
            while (changed) {
                changed = false;
                for (final ModuleCandidate candidate : candidates) {
                    final String id = candidate.descriptor().id();
                    if (rejected.contains(id)) {
                        continue;
                    }
                    boolean rejectedNow = false;
                    for (final String dep : candidate.descriptor().requiredDependencies()) {
                        if (rejected.contains(dep)) {
                            rejected.add(id);
                            changed = true;
                            rejectedNow = true;
                            break;
                        }
                    }
                    if (rejectedNow) {
                        continue;
                    }
                    for (final String cap : candidate.descriptor().requiresCapabilities()) {
                        final Set<String> providers = capabilityProviders.get(cap);
                        if (providers == null) {
                            continue;
                        }
                        boolean anyAlive = false;
                        for (final String provider : providers) {
                            if (!rejected.contains(provider)) {
                                anyAlive = true;
                                break;
                            }
                        }
                        if (!anyAlive) {
                            rejected.add(id);
                            changed = true;
                            break;
                        }
                    }
                }
            }

            candidates = candidates.stream()
                    .filter(c -> !rejected.contains(c.descriptor().id()))
                    .toList();
            byId.keySet().removeAll(rejected);
        }

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<String, Set<String>> adjacency = new HashMap<>();
        final Map<String, Integer> inDegree = new HashMap<>();

        for (final ModuleCandidate candidate : candidates) {
            final String id = candidate.descriptor().id();
            adjacency.putIfAbsent(id, new HashSet<>());
            inDegree.putIfAbsent(id, 0);
        }

        for (final ModuleCandidate candidate : candidates) {
            final ModuleDescriptor desc = candidate.descriptor();
            final String selfId = desc.id();

            for (final String dep : desc.requiredDependencies()) {
                if (byId.containsKey(dep)) {
                    if (adjacency.computeIfAbsent(dep, k -> new HashSet<>()).add(selfId)) {
                        inDegree.merge(selfId, 1, Integer::sum);
                    }
                }
            }

            for (final String dep : desc.optionalDependencies()) {
                if (byId.containsKey(dep)) {
                    if (adjacency.computeIfAbsent(dep, k -> new HashSet<>()).add(selfId)) {
                        inDegree.merge(selfId, 1, Integer::sum);
                    }
                }
            }

            for (final String cap : desc.requiresCapabilities()) {
                final Set<String> providers = capabilityProviders.get(cap);
                if (providers == null) {
                    continue;
                }
                for (final String provider : providers) {
                    if (provider.equals(selfId)) {
                        continue;
                    }
                    if (adjacency.computeIfAbsent(provider, k -> new HashSet<>()).add(selfId)) {
                        inDegree.merge(selfId, 1, Integer::sum);
                    }
                }
            }
        }

        final PriorityQueue<String> queue = new PriorityQueue<>((a, b) -> {
            final int phaseA = byId.get(a).descriptor().loadPhase().ordinal();
            final int phaseB = byId.get(b).descriptor().loadPhase().ordinal();
            return Integer.compare(phaseA, phaseB);
        });
        for (final Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        final List<ModuleCandidate> sorted = new ArrayList<>();
        while (!queue.isEmpty()) {
            final String current = queue.poll();
            sorted.add(byId.get(current));
            for (final String neighbor : adjacency.getOrDefault(current, Collections.emptySet())) {
                final int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (sorted.size() < candidates.size()) {
            final Set<String> remaining = new LinkedHashSet<>(byId.keySet());
            sorted.forEach(c -> remaining.remove(c.descriptor().id()));
            throw new ServiceException("Circular module dependency detected among: " + remaining);
        }

        return sorted;
    }
}
