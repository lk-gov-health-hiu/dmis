/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ejb;

import java.io.Serializable;
import javax.ejb.Stateless;
import org.jasypt.util.text.StrongTextEncryptor;

/**
 * Reversible encryption for sensitive secrets that must be recovered later
 * (currently each user's Claude API key).
 *
 * <p>Uses Jasypt's {@link StrongTextEncryptor} (PBE with MD5 and TripleDES) —
 * the same library the application already uses for its other reversible
 * encryption, but stronger than the legacy {@code BasicTextEncryptor} used for
 * non-sensitive values.</p>
 *
 * <p>The passphrase is resolved, in order, from the system property
 * {@code dmis.crypto.secret}, then the environment variable
 * {@code DMIS_CRYPTO_SECRET}, falling back to a built-in default. Operators
 * should set one of the former in production so secrets are not recoverable
 * from a database dump alone.</p>
 */
@Stateless
public class CryptoService implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final String SYSTEM_PROPERTY = "dmis.crypto.secret";
    private static final String ENV_VARIABLE = "DMIS_CRYPTO_SECRET";
    private static final String DEFAULT_SECRET = "dmis-default-secret-change-me";

    private StrongTextEncryptor encryptor;

    private StrongTextEncryptor encryptor() {
        if (encryptor == null) {
            StrongTextEncryptor e = new StrongTextEncryptor();
            e.setPassword(resolveSecret());
            encryptor = e;
        }
        return encryptor;
    }

    private String resolveSecret() {
        String secret = System.getProperty(SYSTEM_PROPERTY);
        if (secret == null || secret.trim().isEmpty()) {
            secret = System.getenv(ENV_VARIABLE);
        }
        if (secret == null || secret.trim().isEmpty()) {
            secret = DEFAULT_SECRET;
        }
        return secret;
    }

    /**
     * Encrypts the given clear-text value. Returns {@code null} for a
     * {@code null} input.
     */
    public String encrypt(String clearText) {
        if (clearText == null) {
            return null;
        }
        return encryptor().encrypt(clearText);
    }

    /**
     * Decrypts a value previously produced by {@link #encrypt(String)}. Returns
     * {@code null} for a {@code null} input.
     */
    public String decrypt(String cipherText) {
        if (cipherText == null) {
            return null;
        }
        return encryptor().decrypt(cipherText);
    }
}
