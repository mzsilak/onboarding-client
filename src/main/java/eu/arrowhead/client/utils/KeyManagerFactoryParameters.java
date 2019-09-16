package eu.arrowhead.client.utils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

import static eu.arrowhead.client.utils.SSLContextConfigurator.*;

public class KeyManagerFactoryParameters extends AbstractFactoryParameters<KeyManagerFactory>
{
    private final Logger logger = LogManager.getLogger();

    private char[] keyPass;

    public KeyManagerFactoryParameters()
    {
        super();
    }

    KeyManagerFactoryParameters(final boolean readSystemProperties)
    {
        super(readSystemProperties);
    }

    public KeyManagerFactoryParameters(final String storeProvider, final String storeType, final char[] keyPass,
                                       final char[] storePass,
                                       final String storeFile, final byte[] storeBytes,
                                       final String managerFactoryAlgorithm)
    {
        super(storeProvider, storeType, storePass, storeFile, storeBytes, managerFactoryAlgorithm);
        this.keyPass = keyPass;
    }

    @Override
    void loadProperties(final Properties props)
    {
        storeProvider = props.getProperty(KEY_STORE_PROVIDER);
        storeType = props.getProperty(KEY_STORE_TYPE);

        if (props.getProperty(KEY_STORE_PASSWORD) != null)
        {
            storePass = props.getProperty(KEY_STORE_PASSWORD).toCharArray();
        }
        else
        {
            storePass = null;
        }

        storeFile = props.getProperty(KEY_STORE_FILE);
        storeBytes = null;
    }

    @Override
    KeyManagerFactory initFactory(final boolean throwException)
    {
        final KeyManagerFactory keyManagerFactory;
        final KeyStore keyStore;

        try
        {
            keyStore = loadStore();

            String kmfAlgorithm = managerFactoryAlgorithm;
            if (kmfAlgorithm == null)
            {
                kmfAlgorithm = System.getProperty(
                        KEY_FACTORY_MANAGER_ALGORITHM,
                        TrustManagerFactory.getDefaultAlgorithm());
            }

            keyManagerFactory = KeyManagerFactory.getInstance(kmfAlgorithm);
            char[] pass = getOrDefault(keyPass, storePass);
            keyManagerFactory.init(keyStore, getOrDefault(keyPass, storePass));
        }
        catch (KeyStoreException e)
        {
            logger.log(Level.WARN, "Error initializing key store", e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        catch (CertificateException e)
        {
            logger.log(Level.WARN, "Key store certificate exception.", e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        catch (UnrecoverableKeyException e)
        {
            logger.log(Level.WARN, "Key store unrecoverable exception.", e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        catch (FileNotFoundException e)
        {
            logger.log(Level.WARN, "Can't find key store file: {}", storeFile, e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        catch (IOException e)
        {
            logger.log(Level.WARN, "Error loading key store from file: {}", storeFile, e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.WARN, "Error initializing key manager factory (no such algorithm)", e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        catch (NoSuchProviderException e)
        {
            logger.log(Level.WARN, "Error initializing key store (no such provider)", e);
        }


        return null;
    }

    public char[] getKeyPass()
    {
        return keyPass;
    }

    public void setKeyPass(final char[] keyPass)
    {
        this.keyPass = keyPass;
    }
}
