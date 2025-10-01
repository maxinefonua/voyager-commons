package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.currency.Currency;
import org.voyager.model.language.Language;
import org.voyager.model.language.LanguageForm;

import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;
import static org.voyager.utils.ConstantsUtils.*;

public class LanguageService {
    private final String servicePath;
    private final String languagePath;

    LanguageService(VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getLanguagesPath();
        this.languagePath = voyagerConfig.getLanguagePath();
    }

    public Either<ServiceError, List<Language>> getLanguages() {
        return fetch(servicePath, HttpMethod.GET,new TypeReference<List<Language>>(){});
    }

    public Either<ServiceError, Language> addLanguage(LanguageForm languageForm) {
        return fetchWithRequestBody(servicePath, HttpMethod.POST,Language.class,languageForm);
    }

    public Either<ServiceError, Language> getLanguageByIso6391(String iso6391) {
        String requestURL = languagePath.concat(String.format("?%s=%s",LANGUAGE_ISO6391_PARAM_NAME,iso6391));
        return fetch(requestURL, HttpMethod.GET,Language.class);
    }

    public Either<ServiceError, Language> getLanguageByIso6392(String iso6392) {
        String requestURL = languagePath.concat(String.format("?%s=%s",LANGUAGE_ISO6392_PARAM_NAME,iso6392));
        return fetch(requestURL, HttpMethod.GET,Language.class);
    }

    public Either<ServiceError, Language> getLanguageByIso6393(String iso6393) {
        String requestURL = languagePath.concat(String.format("?%s=%s",LANGUAGE_ISO6393_PARAM_NAME,iso6393));
        return fetch(requestURL, HttpMethod.GET,Language.class);
    }
}
