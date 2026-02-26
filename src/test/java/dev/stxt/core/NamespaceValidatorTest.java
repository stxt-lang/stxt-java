package dev.stxt.core;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import dev.stxt.NamespaceValidator;
import dev.stxt.exceptions.ParseException;

class NamespaceValidatorTest
{
    @Test
    void testNamespace() {
        assertNull(null);
        
        checkValid("com.demo");
        checkValid("@com.demo");
        checkValid("");
        
        checkInvalid("com");
        checkInvalid("com.");
        checkInvalid("com.demo.");
        checkInvalid("com.demoX");
    }

    private void checkInvalid(String string) {
        try {
            NamespaceValidator.validateNamespaceFormat(string, 0);
            fail("Debería haber saltado excepción con '" + string + "'");
        } catch (ParseException e) {
            System.out.println("OK Error con " + string);
        }        
    }

    private void checkValid(String string) {
        NamespaceValidator.validateNamespaceFormat(string, 0);
        System.out.println("OK con '" + string + "'");
    }
}
