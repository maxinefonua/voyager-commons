package org.voyager;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.model.language.Language;
import org.voyager.model.language.LanguageForm;
import org.voyager.model.language.LanguageISO;
import org.voyager.service.GeoNamesService;
import org.voyager.service.LanguageService;
import org.voyager.service.Voyager;
import org.voyager.utils.ConstantsLocal;
import org.voyager.utils.DatasyncProgramArguments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.voyager.utils.ConstantsUtils.ALPHA3_CODE_REGEX;

public class LanguageSync {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageSync.class);
    private static LanguageService languageService;

    public static void main(String[] args) {

        // TODO: use this as source: https://download.geonames.org/export/dump/iso-languagecodes.txt

        LOGGER.info("printing from language sync main");
        DatasyncProgramArguments datasyncProgramArguments = new DatasyncProgramArguments(args);
        Integer maxConcurrentRequests = datasyncProgramArguments.getThreadCount();
        String host = datasyncProgramArguments.getHostname();
        int port = datasyncProgramArguments.getPort();
        String voyagerAuthorizationToken = datasyncProgramArguments.getAccessToken();
        int processLimit = datasyncProgramArguments.getProcessLimit();
        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,host,port,maxConcurrentRequests,voyagerAuthorizationToken);
        Voyager voyager = new Voyager(voyagerConfig);
        languageService = voyager.getLanguageService();
        Either<ServiceError,List<LanguageISO>> languageISOEither = GeoNamesService.getLanguageISOList();
        if (languageISOEither.isLeft()) {
            Exception exception = languageISOEither.getLeft().getException();
            throw new RuntimeException(String.format("failed to load languageISO list from GeoNamesService, error: %s",
                    exception.getMessage()),exception);
        }
        List<LanguageISO> loadedLanguages = languageISOEither.get();
        LOGGER.info(String.format("loaded %d languages from GeoNamesService pre-processing",
                loadedLanguages.size()));
        Either<ServiceError, List<Language>> either = languageService.getLanguages();
        if (either.isLeft()) {
            Exception exception = either.getLeft().getException();
            throw new RuntimeException(String.format("failed to fetch languages from Voyager API, error: %s",
                    exception.getMessage()),exception);
        }
        Map<String,Language> nameToLangMap = either.get().stream().collect(
                Collectors.toMap(Language::getName,language -> language));
        LOGGER.info(String.format("fetched %d languages from Voyager to filter pre-processing",
                nameToLangMap.size()));

        List<LanguageISO> toProcess = loadedLanguages.stream()
                .filter(languageISO -> !nameToLangMap.containsKey(languageISO.getName()))
                .limit(processLimit).toList();
        LOGGER.info(String.format("attempting to process %d languages post-filtering",
                toProcess.size()));
        Integer filtered = loadedLanguages.size() - toProcess.size();

        List<LanguageForm> failedAdds = new ArrayList<>();
        AtomicInteger added = new AtomicInteger(0);

        toProcess.forEach(languageISO -> {
            LanguageForm languageForm = LanguageForm.builder()
                    .name(languageISO.getName())
                    .iso6391(languageISO.getAlpha639code1())
                    .iso6392(languageISO.getAlpha639code2())
                    .iso6393(languageISO.getAlpha639code3())
                    .build();
            if (StringUtils.isNotBlank(languageForm.getIso6392()) && languageForm.getIso6392().length() != 3
                    && languageForm.getIso6392().contains(languageForm.getIso6393()))
                languageForm.setIso6392(languageForm.getIso6393());
            Either<ServiceError,Language> addEither = languageService.addLanguage(languageForm);
            if (addEither.isLeft()) {
                Exception exception = addEither.getLeft().getException();
                LOGGER.error(String.format("failed to add %s, error: %s",languageForm,exception.getMessage()));
                failedAdds.add(languageForm);
                return;
            }
            LOGGER.info(String.format("successfully added language: %s",addEither.get()));
            added.getAndIncrement();
        });
        failedAdds.forEach(languageForm -> LOGGER.info(
                String.format("failed to add: %s",languageForm)));
        LOGGER.info(String.format("completed processing with %d failed adds, %d filtered languages, and %d added languages",
                failedAdds.size(),filtered,added.get()));
    }
}