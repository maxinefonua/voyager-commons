package org.voyager;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.currency.*;
import org.voyager.model.currency.Currency;
import org.voyager.service.CountryService;
import org.voyager.service.CurrencyService;
import org.voyager.service.Voyager;
import org.voyager.service.currency.APIVerveService;
import org.voyager.service.currency.OpenExchangeRatesService;
import org.voyager.service.currency.XeCurrencyService;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.DatasyncProgramArguments;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.voyager.utils.ConstantsUtils.ENGLISH_APLHA_REGEX;

public class CurrencySync {
    private static CurrencyService currencyService;
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencySync.class);
    private static final int SKIP_COUNT = 0;

    public static void main(String[] args) {
        LOGGER.info("printing from currency sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        int processLimit = datasyncProgramArguments.getProcessLimit();
        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);
        Voyager voyager = new Voyager(voyagerConfig);
        currencyService = voyager.getCurrencyService();

        Either<ServiceError,CurrencyNames> currencyNamesEither = OpenExchangeRatesService.fetchCurrencyNames();
        if (currencyNamesEither.isLeft()) {
            Exception exception = currencyNamesEither.getLeft().getException();
            String message = String.format("failed to fetch currency names from OpenExchangeRatesService; error: %s",
                    exception.getMessage());
            throw new RuntimeException(message,exception);
        }

        Either<ServiceError, CurrencyRates> currencyRatesEither = OpenExchangeRatesService.fetchCurrencyRates();
        if (currencyRatesEither.isLeft()) {
            Exception exception = currencyRatesEither.getLeft().getException();
            String message = String.format("failed to fetch currency names from OpenExchangeRatesService; error: %s",
                    exception.getMessage());
            throw new RuntimeException(message,exception);
        }
        CurrencyNames currencyNames = currencyNamesEither.get();
        Map<String,String> mappedNames = currencyNames.getMappedNames();
        CurrencyRates currencyRates = currencyRatesEither.get();
        Map<String,Double> mappedRates = currencyRates.getMappedRates();
        LOGGER.info(String.format("fetched %d currencies to process, with %d current rates",mappedNames.size(),mappedRates.size()));

        Map<String,Currency> currencyMap = new HashMap<>();
        Either<ServiceError, List<Currency>> currenciesEither = currencyService.getCurrencies();
        if (currenciesEither.isLeft()) {
            Exception exception = currenciesEither.getLeft().getException();
            throw new RuntimeException("Get currencies returned exception: ",exception);
        }
        currenciesEither.get().forEach(currency -> {
            currencyMap.put(currency.getCode(),currency);
        });
        LOGGER.info(String.format("fetched %d existing currencies via Voyager API",currencyMap.size()));

        Set<String> loadedCodesFromFile = ConstantsLocal.loadCodesFromListFile(ConstantsLocal.CURRENCY_RETRIES_FILE).stream().limit(processLimit).collect(Collectors.toSet());
        Map<String,String> toProcess = new HashMap<>();
        for (String code : loadedCodesFromFile) {
            if (mappedNames.containsKey(code))
                toProcess.put(code, mappedNames.get(code));
        }

        AtomicInteger skippedMatches = toProcess.isEmpty() ? new AtomicInteger(SKIP_COUNT) : new AtomicInteger(0);
        if (toProcess.isEmpty()) toProcess = mappedNames.entrySet().stream()
                .skip(SKIP_COUNT)
                .limit(processLimit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (v1, v2) -> v1, // merge function for duplicate keys
                        LinkedHashMap::new // preserves insertion order
                ));
        LOGGER.info(String.format("attempting to process %d total currencies, skipped %d previous currencies",toProcess.size(),SKIP_COUNT));

        List<String> patchFailures = new ArrayList<>();
        List<String> addFailures = new ArrayList<>();
        List<String> symbolFailures = new ArrayList<>();
        AtomicInteger successfulPatches = new AtomicInteger(0);
        AtomicInteger successfulAdds = new AtomicInteger(0);

        Map<String,CompletableFuture<Void>> completableFutureList = new HashMap<>();
        toProcess.forEach((code,name) -> {
            Double newRate = mappedRates.get(code);
            Currency currency = currencyMap.get(code);
            if (currency != null) {
                CompletableFuture<Void> patchCF = CompletableFuture.runAsync(() -> updateExistingCurrency(currency,newRate,patchFailures,successfulPatches,symbolFailures,skippedMatches));
                completableFutureList.put(code,patchCF);
            } else {
                CompletableFuture<Void> addCF = CompletableFuture.runAsync(() -> addNewCurrency(code,name,newRate,addFailures,successfulAdds,symbolFailures));
                completableFutureList.put(code,addCF);
            }
        });
        List<String> getFailures = new ArrayList<>();
        completableFutureList.forEach((code, cf) -> {
            try {
                cf.get();
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error(String.format("failed to complete future of '%s'",code));
                getFailures.add(code);
            }
        });

        Set<String> retries = new HashSet<>();
        retries.addAll(patchFailures);
        retries.addAll(addFailures);
        retries.addAll(symbolFailures);
        retries.addAll(getFailures);

        LOGGER.info(String.format("processed %d total currencies, " +
                        "with %d patch failures, %d patch successes, %d symbol failures, " +
                        "%d add failures, %d add successes, %d complete future failures, and %d skipped currencies",
                toProcess.size(),patchFailures.size(),successfulPatches.get(),symbolFailures.size(),
                addFailures.size(),successfulAdds.get(),getFailures.size(),skippedMatches.get()));
        ConstantsLocal.writeSetLineByLine(retries,ConstantsLocal.CURRENCY_RETRIES_FILE);
    }

    private static String resolveSymbol(String code,List<String> symbolFailures) {
        String symbol;
        try {
            symbol = java.util.Currency.getInstance(code).getSymbol();
            if (!symbol.equals(code)) return symbol;
        } catch (IllegalArgumentException e) {
            LOGGER.error(String.format("failed to fetch symbol '%s' from Java Currency utils, error: %s, " +
                    "attempting to fetch from APIVerve",code,e.getMessage()));
        }
        symbol = XeCurrencyService.extractCurrencySymbol(code);
        if (!symbol.equals(code)) return symbol;
        return APIVerveService.extractCurrencySymbol(code);
    }

    private static void addNewCurrency(String code, String name, Double newRate,
                                       List<String> addFailures, AtomicInteger successfulAdds, List<String> symbolFailures) {
        String symbol = resolveSymbol(code,symbolFailures);
        LOGGER.info(String.format("resolved symbol '%s' for currency '%s'",symbol,code));
        CurrencyForm currencyForm = CurrencyForm.builder()
                .code(code)
                .name(name)
                .usdRate(newRate)
                .isActive(newRate != null)
                .symbol(symbol)
                .build();
        Either<ServiceError,Currency> addEither = currencyService.addCurrency(currencyForm);
        if (addEither.isLeft()) {
            Exception exception = addEither.getLeft().getException();
            LOGGER.error(String.format("Failed to add currency '%s' with error: %s",code,exception.getMessage()));
            addFailures.add(code);
        } else {
            successfulAdds.getAndIncrement();
            LOGGER.info(String.format("successfully added currency: %s", addEither.get()));
        }
    }

    private static void updateExistingCurrency(Currency currency, Double newRate,
                                               List<String> patchFailures, AtomicInteger successfulPatches, List<String> symbolFailures, AtomicInteger skippedMatches) {
        CurrencyPatch currencyPatch = CurrencyPatch.builder().build();
        boolean updated = false;
        String code = currency.getCode();
        String symbol = currency.getSymbol();
        if (StringUtils.isBlank(symbol) || symbol.equals(code) ||
                symbol.matches(ENGLISH_APLHA_REGEX)) {
            String fetchedSymbol = resolveSymbol(code,symbolFailures);
            LOGGER.info(String.format("resolved symbol '%s' for currency '%s'",symbol,code));
            if (!fetchedSymbol.equals(symbol)) {
                currencyPatch.setSymbol(symbol);
                updated = true;
            } else symbolFailures.add(code);
        }
        if (newRate == null) {
            currencyPatch.setIsActive(false);
            currencyPatch.setUsdRate(newRate);
            updated = true;
        } else if (!currency.getUsdRate().equals(newRate)) {
            currencyPatch.setUsdRate(newRate);
            updated = true;
        }
        if (updated) {
            Either<ServiceError,Currency> patchEither = currencyService.patchCurrency(code,currencyPatch);
            if (patchEither.isLeft()) {
                Exception exception = patchEither.getLeft().getException();
                LOGGER.error(String.format("Failed to patch currency '%s' with patch: %s, error: %s",
                        code,currencyPatch,exception.getMessage()));
                patchFailures.add(code);
            } else {
                successfulPatches.getAndIncrement();
                LOGGER.info(String.format("successfully patched existing currency: %s", patchEither.get()));
            }
        } else {
            LOGGER.info(String.format("skipping existing currency with no udpates: %s",currency));
            skippedMatches.getAndIncrement();
        }
    }
}
