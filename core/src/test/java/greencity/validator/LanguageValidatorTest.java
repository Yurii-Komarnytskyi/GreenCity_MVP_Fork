package greencity.validator;

import greencity.service.LanguageService;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LanguageValidatorTest {

    @Mock
    private LanguageService languageService;

    @Mock
    private ConstraintValidatorContext constraintValidatorContext;

    @InjectMocks
    private LanguageValidator languageValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        List<String> languageCodes = Arrays.asList("en", "ua", "fr");
        when(languageService.findAllLanguageCodes()).thenReturn(languageCodes);
        languageValidator.initialize(null);
    }

    @Test
    void testIsValidWithValidLanguage() {
        Locale locale = Locale.forLanguageTag("en");
        assertTrue(languageValidator.isValid(locale, constraintValidatorContext));
    }

    @Test
    void testIsValidWithInvalidLanguage() {
        Locale locale = Locale.forLanguageTag("de");
        assertFalse(languageValidator.isValid(locale, constraintValidatorContext));
    }

}
