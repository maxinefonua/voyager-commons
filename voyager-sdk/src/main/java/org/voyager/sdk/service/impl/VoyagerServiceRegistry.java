package org.voyager.sdk.service.impl;

import org.voyager.sdk.config.VoyagerConfig;
import org.voyager.sdk.http.VoyagerHttpFactory;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VoyagerServiceRegistry {
    private static final VoyagerServiceRegistry INSTANCE = new VoyagerServiceRegistry();
    private static final Map<Class<?>,Object> services = new ConcurrentHashMap<>();
    private static boolean initialized = false;
    private static boolean testMode = false;

    protected VoyagerServiceRegistry() {}

    public static void initialize(VoyagerConfig voyagerConfig) {
        if (!initialized) {
            ServiceUtilsFactory.initialize(voyagerConfig);
            VoyagerHttpFactory.initialize(voyagerConfig.getAuthorizationToken());
            ServiceUtilsFactory.getInstance().verifyHealth();
            initialized = true;
        }
    }

    private static void checkInitialized() {
        if (!testMode && !initialized) {
            throw new IllegalStateException("VoyagerServiceRegistry not initialized. Call initialize() first.");
        }
    }

    public static VoyagerServiceRegistry getInstance() {
        checkInitialized();
        return INSTANCE;
    }

    protected <T> void registerTestImplementation(Class<T> interfaceClass, Class<? extends T> implementationClass,
                                               ServiceUtils serviceUtils) {
        testMode = true;
        services.computeIfAbsent(interfaceClass,k->{
            try {
                return implementationClass.getDeclaredConstructor(ServiceUtils.class).newInstance(serviceUtils);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException("Failed to create service: " + implementationClass.getName(), e);
            }
        });
    }

    public void reset(){
        services.clear();
        testMode = false;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> serviceClass) {
        checkInitialized();
        T service = (T) services.get(serviceClass);
        if (service == null) {
            return switch (serviceClass.getSimpleName()) {
                case "AirlineService" -> {
                    services.put(serviceClass, new AirlineServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                case "AirportService" -> {
                    services.put(serviceClass, new AirportServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                case "CountryService" -> {
                    services.put(serviceClass, new CountryServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                case "FlightService" -> {
                    services.put(serviceClass, new FlightServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                case "RouteService" -> {
                    services.put(serviceClass, new RouteServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                case "SearchService" -> {
                    services.put(serviceClass, new SearchServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                case "GeoService" -> {
                    services.put(serviceClass, new GeoServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                case "RouteSyncService" -> {
                    services.put(serviceClass, new RouteSyncServiceImpl());
                    yield (T) services.get(serviceClass);
                }
                default ->
                        throw new IllegalStateException("No implementation registered for type: " + serviceClass.getSimpleName());
            };
        }
        return service;
    }
}
