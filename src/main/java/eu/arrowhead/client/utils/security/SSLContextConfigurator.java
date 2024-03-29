package eu.arrowhead.client.utils.security;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Properties;

public class SSLContextConfigurator
{
    public final static String DEFAULT_SECURITY_PROTOCOL = "TLS";

    static final String TRUST_STORE_PROVIDER = "javax.net.ssl.trustStoreProvider";
    static final String KEY_STORE_PROVIDER = "javax.net.ssl.keyStoreProvider";
    static final String TRUST_STORE_FILE = "javax.net.ssl.trustStore";
    static final String KEY_STORE_FILE = "javax.net.ssl.keyStore";
    static final String TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
    static final String KEY_STORE_PASSWORD = "javax.net.ssl.keyStorePassword";
    static final String TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType";
    static final String KEY_STORE_TYPE = "javax.net.ssl.keyStoreType";
    static final String KEY_FACTORY_MANAGER_ALGORITHM = "ssl.KeyManagerFactory.algorithm";
    static final String TRUST_FACTORY_MANAGER_ALGORITHM = "ssl.TrustManagerFactory.algorithm";

    private final Logger logger = LogManager.getLogger();

    private final KeyManagerFactoryParameters kmfParameters;
    private final TrustManagerFactoryParameters tmfParameters;

    private String securityProtocol = DEFAULT_SECURITY_PROTOCOL;

    private KeyManagerFactory keyManagerFactory;
    private TrustManagerFactory trustManagerFactory;

    /**
     * Default constructor. Reads configuration properties from
     * {@link System#getProperties()}. Calls {@link #SSLContextConfigurator(boolean)} with
     * <code>true</code>.
     */
    public SSLContextConfigurator()
    {
        this(true);
    }

    /**
     * Constructor that allows you creating empty configuration.
     *
     * @param readSystemProperties If <code>true</code> populates configuration from
     *                             {@link System#getProperties()}, else you have empty
     *                             configuration.
     */
    public SSLContextConfigurator(boolean readSystemProperties)
    {
        kmfParameters = new KeyManagerFactoryParameters(readSystemProperties);
        tmfParameters = new TrustManagerFactoryParameters(readSystemProperties);
    }

    public void loadProperties(Properties props)
    {
        kmfParameters.loadProperties(props);
        tmfParameters.loadProperties(props);
    }

    public KeyManagerFactoryParameters getKeyManagerFactoryParameters()
    {
        return kmfParameters;
    }

    public TrustManagerFactoryParameters getTrustManagerFactoryParameters()
    {
        return tmfParameters;
    }

    /**
     * Create a new {@link SSLContext}.  If the {@link SSLContext} cannot be created for whatever reason,
     * a {@link GenericStoreException}
     * will be raised containing the root cause of the failure.
     *
     * @param throwException <code>true</code> if an exception should be raised upon failure.
     * @return a new {@link SSLContext}
     *
     * @throws GenericStoreException <code>throwException</code> is <code>true</code> and
     *                               the SSLContext cannot be created
     */
    public SSLContext createSSLContext(final boolean throwException)
    {
        final KeyManagerFactory keyManagerFactory = createKeyManagerFactory();
        final TrustManagerFactory trustManagerFactory = createTrustManagerFactory();
        return createSSLContext(throwException, keyManagerFactory, trustManagerFactory);
    }

    public SSLContext createSSLContext(final boolean throwException, final KeyManager[] keyManagers, final TrustManager[] trustManagers)
    {
        SSLContext sslContext = null;

        try
        {
            final String secProtocol = Objects.nonNull(securityProtocol) ? securityProtocol : DEFAULT_SECURITY_PROTOCOL;
            sslContext = SSLContext.getInstance(secProtocol);
            sslContext.init(keyManagers, trustManagers, null);
        }
        catch (KeyManagementException e)
        {
            logger.log(Level.WARN, "Key management error.", e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            logger.log(Level.WARN, "Error initializing algorithm.", e);
            if (throwException)
            {
                throw new GenericStoreException(e);
            }
        }
        return sslContext;
    }

    public SSLContext createSSLContext(final boolean throwException, final KeyManagerFactory keyManagerFactory, final TrustManagerFactory trustManagerFactory)
    {
        final KeyManagerFactory kmf = keyManagerFactory != null ? keyManagerFactory : createKeyManagerFactory();
        final TrustManagerFactory tmf = trustManagerFactory != null ? trustManagerFactory : createTrustManagerFactory();

        return createSSLContext(throwException,
                                keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                                trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null);
    }

    public SSLContext createTrustAllSSLContext(final boolean throwException)
    {
        final KeyManagerFactory keyManagerFactory = createKeyManagerFactory();
        final KeyManager[] keyManagers = Objects.nonNull(keyManagerFactory) ? keyManagerFactory.getKeyManagers() : null;
        final TrustManager[] trustAllCerts = new TrustManager[]{new TrustAllX509TrustManager()};
        return createSSLContext(throwException, keyManagers, trustAllCerts);
    }

    public KeyManagerFactory createKeyManagerFactory()
    {
        return kmfParameters.initFactory(true);
    }

    public TrustManagerFactory createTrustManagerFactory()
    {
        return tmfParameters.initFactory(true);
    }

    public static class NoopHostnameVerifier implements HostnameVerifier
    {
        /**
         * Singleton instance.
         */
        public static final HostnameVerifier INSTANCE = new NoopHostnameVerifier();

        private NoopHostnameVerifier() { /* NOOP */ }

        @Override
        public boolean verify(final String s, final SSLSession sslSession)
        {
            return true;
        }
    }

    public static class TrustAllX509TrustManager implements X509TrustManager
    {
        public X509Certificate[] getAcceptedIssuers()
        {
            return new X509Certificate[0];
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
        {
            // void;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
        {
            // void;
        }
    }

    /**
     * Sets the <em>trust</em> store provider name.
     *
     * @param trustStoreProvider <em>Trust</em> store provider to set.
     */
    public void setTrustStoreProvider(String trustStoreProvider)
    {
        tmfParameters.setStoreProvider(trustStoreProvider);
    }

    /**
     * Sets the <em>key</em> store provider name.
     *
     * @param keyStoreProvider <em>Key</em> store provider to set.
     */
    public void setKeyStoreProvider(String keyStoreProvider)
    {
        kmfParameters.setStoreProvider(keyStoreProvider);
    }

    /**
     * Type of <em>trust</em> store.
     *
     * @param trustStoreType Type of <em>trust</em> store to set.
     */
    public void setTrustStoreType(String trustStoreType)
    {
        tmfParameters.setStoreType(trustStoreType);
    }

    /**
     * Type of <em>key</em> store.
     *
     * @param keyStoreType Type of <em>key</em> store to set.
     */
    public void setKeyStoreType(String keyStoreType)
    {
        kmfParameters.setStoreType(keyStoreType);
    }

    /**
     * Password of <em>trust</em> store.
     *
     * @param trustStorePass Password of <em>trust</em> store to set.
     */
    public void setTrustStorePassword(String trustStorePass)
    {
        setTrustStorePassword(trustStorePass.toCharArray());
    }

    /**
     * Password of <em>trust</em> store.
     *
     * @param trustStorePass Password of <em>trust</em> store to set.
     */
    public void setTrustStorePassword(char[] trustStorePass)
    {
        tmfParameters.setStorePassword(trustStorePass);
    }

    /**
     * Password of <em>key</em> store.
     *
     * @param keyStorePass Password of <em>key</em> store to set.
     */
    public void setKeyStorePassword(String keyStorePass)
    {
        setKeyStorePassword(keyStorePass.toCharArray());
    }

    /**
     * Password of <em>key</em> store.
     *
     * @param keyStorePass Password of <em>key</em> store to set.
     */
    public void setKeyStorePassword(char[] keyStorePass)
    {
        kmfParameters.setStorePassword(keyStorePass);
    }

    /**
     * Password of the key in the <em>key</em> store.
     *
     * @param keyPass Password of <em>key</em> to set.
     */
    public void setKeyPassword(String keyPass)
    {
        setKeyPassword(keyPass.toCharArray());
    }

    /**
     * Password of the key in the <em>key</em> store.
     *
     * @param keyPass Password of <em>key</em> to set.
     */
    public void setKeyPassword(char[] keyPass)
    {
        kmfParameters.setKeyPassword(keyPass);
    }

    /**
     * Sets trust store file name, also makes sure that if other trust store
     * configuration parameters are not set to set them to default values.
     * Method resets trust store bytes if any have been set before via
     * {@link #setTrustStoreBytes(byte[])}.
     *
     * @param trustStoreFile File name of trust store.
     */
    public void setTrustStoreFile(String trustStoreFile)
    {
        tmfParameters.setStoreFileName(trustStoreFile);
    }

    /**
     * Sets trust store payload as byte array.
     * Method resets trust store file if any has been set before via
     * {@link #setTrustStoreFile(java.lang.String)}.
     *
     * @param trustStoreBytes trust store payload.
     */
    public void setTrustStoreBytes(byte[] trustStoreBytes)
    {
        tmfParameters.setStoreBytes(trustStoreBytes);
    }

    /**
     * Sets key store file name, also makes sure that if other key store
     * configuration parameters are not set to set them to default values.
     * Method resets key store bytes if any have been set before via
     * {@link #setKeyStoreBytes(byte[])}.
     *
     * @param keyStoreFile File name of key store.
     */
    public void setKeyStoreFile(String keyStoreFile)
    {
        kmfParameters.setStoreFileName(keyStoreFile);
    }

    /**
     * Sets key store payload as byte array.
     * Method resets key store file if any has been set before via
     * {@link #setKeyStoreFile(java.lang.String)}.
     *
     * @param keyStoreBytes key store payload.
     */
    public void setKeyStoreBytes(byte[] keyStoreBytes)
    {
        kmfParameters.setStoreBytes(keyStoreBytes);
    }

    /**
     * Sets the trust manager factory algorithm.
     *
     * @param trustManagerFactoryAlgorithm the trust manager factory algorithm.
     */
    public void setTrustManagerFactoryAlgorithm(String trustManagerFactoryAlgorithm)
    {
        tmfParameters.setManagerFactoryAlgorithm(trustManagerFactoryAlgorithm);
    }

    /**
     * Sets the key manager factory algorithm.
     *
     * @param keyManagerFactoryAlgorithm the key manager factory algorithm.
     */
    public void setKeyManagerFactoryAlgorithm(String keyManagerFactoryAlgorithm)
    {
        kmfParameters.setManagerFactoryAlgorithm(keyManagerFactoryAlgorithm);
    }

    /**
     * Sets the SSLContext protocol. The default value is <code>TLS</code> if
     * this is null.
     *
     * @param securityProtocol Protocol for {@link javax.net.ssl.SSLContext#getProtocol()}.
     */
    public void setSecurityProtocol(String securityProtocol)
    {
        this.securityProtocol = securityProtocol;
    }

    final static class GenericStoreException extends RuntimeException
    {
        GenericStoreException(final Throwable e) {super(e.getMessage(), e);}
    }
}
