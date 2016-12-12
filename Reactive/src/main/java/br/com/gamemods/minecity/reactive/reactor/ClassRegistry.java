package br.com.gamemods.minecity.reactive.reactor;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassRegistry<Subject>
{
    @NotNull
    private final Map<Class<?>, Optional<Subject>> objects = new ConcurrentHashMap<>();

    @NotNull
    private final Map<Class<?>, Optional<Subject>> cache = new ConcurrentHashMap<>();

    public void register(Class<?> key, @NotNull Subject subject)
    {
        Class<Object> obj = Object.class;
        if(Objects.requireNonNull(key, String.format("Tried to register %s with a null key", subject)).equals(obj))
            throw new IllegalArgumentException("Object class cannot be registered!");

        Class<?> superClass = key.getSuperclass();
        Class<?> top = superClass;
        while((superClass = superClass.getSuperclass()) != null)
            top = superClass;

        if(!top.equals(obj))
            throw new IllegalArgumentException(String.format("Only normal object classes can be registered! Tried to register %s with key: %s", subject, key));

        if(objects.containsKey(key))
            throw new IllegalStateException(String.format("The key %s is already registered to %s", key, objects.get(key)));

        objects.put(key, Optional.of(subject));
    }

    @NotNull
    public Optional<Subject> get(Class<?> search)
    {
        return cache.computeIfAbsent(search, this::search);
    }

    @NotNull
    private Optional<Subject> search(Class<?> search)
    {
        Class<?> superclass = search.getSuperclass();
        if(superclass == null)
            return Optional.empty();

        Optional<Subject> subject = objects.getOrDefault(search, Optional.empty());
        if(subject.isPresent())
            return subject;

        return cache.computeIfAbsent(superclass, this::search);
    }
}
