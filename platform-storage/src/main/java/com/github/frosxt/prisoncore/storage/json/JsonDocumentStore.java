package com.github.frosxt.prisoncore.storage.json;

import com.github.frosxt.prisoncore.storage.api.DocumentStore;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public final class JsonDocumentStore<ID, D> implements DocumentStore<ID, D> {
    private final Path directory;
    private final Class<D> documentType;
    private final Gson gson;
    private final Logger logger;

    public JsonDocumentStore(final Path directory, final Class<D> documentType, final Logger logger) {
        this.directory = directory;
        this.documentType = documentType;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.logger = logger;
        try {
            Files.createDirectories(directory);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to create storage directory: " + directory, e);
        }
    }

    @Override
    public Optional<D> find(final ID id) {
        final Path file = filePath(id);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try (final Reader reader = Files.newBufferedReader(file)) {
            return Optional.of(gson.fromJson(reader, documentType));
        } catch (final IOException e) {
            logger.log(Level.WARNING, "Failed to read document: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public Collection<D> findAll() {
        final List<D> results = new ArrayList<>();
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.json")) {
            for (final Path file : stream) {
                try (final Reader reader = Files.newBufferedReader(file)) {
                    results.add(gson.fromJson(reader, documentType));
                }
            }
        } catch (final IOException e) {
            logger.log(Level.WARNING, "Failed to list documents", e);
        }
        return results;
    }

    @Override
    public Collection<D> findWhere(final Predicate<D> filter) {
        return findAll().stream().filter(filter).collect(Collectors.toList());
    }

    @Override
    public void save(final ID id, final D document) {
        final Path file = filePath(id);
        final Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        try (final Writer writer = Files.newBufferedWriter(temp)) {
            gson.toJson(document, writer);
        } catch (final IOException e) {
            logger.log(Level.SEVERE, "Failed to save document: " + id, e);
            return;
        }
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (final IOException e) {
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            } catch (final IOException ex) {
                logger.log(Level.SEVERE, "Failed to finalize document save: " + id, ex);
            }
        }
    }

    @Override
    public void delete(final ID id) {
        try {
            Files.deleteIfExists(filePath(id));
        } catch (final IOException e) {
            logger.log(Level.WARNING, "Failed to delete document: " + id, e);
        }
    }

    private Path filePath(final ID id) {
        return directory.resolve(id.toString() + ".json");
    }
}
