package eu.arrowhead.onboarding.impl;

import eu.arrowhead.onboarding.OnboardingClient;
import eu.arrowhead.client.transport.ProtocolConfiguration;
import eu.arrowhead.client.utils.SSLContextConfigurator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class OnboardingClientBuilder
{
    private final Logger logger = LogManager.getLogger();

    private final ProtocolConfiguration protocol;
    private String address = "localhost";
    private int retries = 3;
    private long delayBetweenRetries = 5;
    private TimeUnit timeUnitForRetries = TimeUnit.SECONDS;

    private SSLContext sslContext;
    private SSLContextConfigurator configurator;
    private TrustManagerFactory trustManagerFactory;
    private KeyManagerFactory keyManagerFactory;

    public OnboardingClientBuilder(final ProtocolConfiguration protocol)
    {
        logger.debug("Creating new {} with protocol {}", getClass().getSimpleName(), protocol);
        this.protocol = protocol;
    }

    ProtocolConfiguration getProtocol()
    {
        return protocol;
    }

    InetAddress getAddress()
    {
        try
        {
            return InetAddress.getByName(address);
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }

    int getRetries()
    {
        return retries;
    }

    long getDelayBetweenRetries()
    {
        return delayBetweenRetries;
    }

    TimeUnit getTimeUnitForRetries()
    {
        return timeUnitForRetries;
    }

    SSLContext getSslContext()
    {
        createSslContext();
        return sslContext;
    }

    SSLContextConfigurator getConfigurator()
    {
        return configurator;
    }

    TrustManagerFactory getTrustManagerFactory()
    {
        return trustManagerFactory;
    }

    KeyManagerFactory getKeyManagerFactory()
    {
        return keyManagerFactory;
    }

    public OnboardingClientBuilder withOnboardingAddress(final String address)
    {
        this.address = address;
        return this;
    }

    public OnboardingClientBuilder withDelayBetweenRetries(final long number, final TimeUnit unit)
    {
        this.delayBetweenRetries = number;
        this.timeUnitForRetries = unit;
        return this;
    }

    public OnboardingClientBuilder withRetries(final int retries)
    {
        this.retries = retries;
        return this;
    }

    public OnboardingClientBuilder withMaximumRetries()
    {
        this.retries = Integer.MAX_VALUE;
        return this;
    }

    public OnboardingClientBuilder withSSLContext(final SSLContext context)
    {
        this.sslContext = context;
        return this;
    }

    public OnboardingClientBuilder withSSLConfiguration(final SSLContextConfigurator configurator)
    {
        this.configurator = configurator;
        return this;
    }

    public OnboardingClientBuilder withSSLFactories(final TrustManagerFactory trustManagerFactory,
                                                    final KeyManagerFactory keyManagerFactory)
    {
        this.trustManagerFactory = trustManagerFactory;
        this.keyManagerFactory = keyManagerFactory;
        return this;
    }

    private void createSslContext()
    {
        try
        {
            if (Objects.isNull(sslContext))
            {
                if (Objects.nonNull(configurator))
                {
                    logger.debug("Creating new {} with configurator {}", SSLContext.class.getSimpleName(), configurator);
                    sslContext = configurator.createSSLContext(true);
                }
                else if (Objects.nonNull(keyManagerFactory) || Objects.nonNull(trustManagerFactory))
                {
                    logger.debug("Creating new {} with KeyManagerFactory {}, and TrustManagerFactory {}",
                                 SSLContext.class.getSimpleName(), keyManagerFactory, trustManagerFactory);
                    sslContext = SSLContext.getInstance("TLS");
                    sslContext.init(keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                                    trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null, null);
                }
                else
                {
                    logger.debug("Creating blank {}", SSLContext.class.getSimpleName());
                    configurator = new SSLContextConfigurator(true);
                    sslContext = configurator.createSSLContext(true);
                }
            }
            else
            {
                logger.debug("Using given {}: {}", SSLContext.class.getSimpleName(), sslContext);
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    public OnboardingClient build()
    {
        return new OnboardingClientImpl(protocol, getAddress(), this);
    }
}
