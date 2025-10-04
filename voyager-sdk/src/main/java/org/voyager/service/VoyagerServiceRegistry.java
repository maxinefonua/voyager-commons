package org.voyager.service;

import org.voyager.config.VoyagerConfig;
import org.voyager.http.VoyagerHttpFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class VoyagerServiceRegistry {
    private static final VoyagerServiceRegistry INSTANCE = new VoyagerServiceRegistry();
    private static final Map<Class<?>,Object> services = new ConcurrentHashMap<>();

    protected VoyagerServiceRegistry() {}

    public static VoyagerServiceRegistry getInstance() {
        return INSTANCE;
    }

    public <T> void registerImplementation(Class<T> interfaceClass, Class<? extends T> implementationClass) {
        services.computeIfAbsent(interfaceClass,k->{
            try {
                return implementationClass.getDeclaredConstructor().newInstance();
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException("Failed to create service: " + implementationClass.getName(), e);
            }
        });
    }

    public <T> void registerSupplier(Class<T> interfaceClass, Supplier<? extends T> implementationSupplier) {
        services.computeIfAbsent(interfaceClass,k->implementationSupplier.get());
    }

    public void reset(){
        services.clear();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceClass) {
        T service = (T) services.get(serviceClass);
        if (service == null) {
            throw new IllegalStateException("No implementation registered for type: " + serviceClass.getName());
        }
        return service;
    }
}
