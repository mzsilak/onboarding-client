package eu.arrowhead.client.misc;

import eu.arrowhead.client.transport.ProtocolConfiguration;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SystemEndpointHolder
{
    private final ConcurrentMap<CoreSystems, URI> systems = new ConcurrentHashMap<>();
    private final ProtocolConfiguration protocolConfiguration;

    public SystemEndpointHolder(final ProtocolConfiguration protocolConfiguration) {this.protocolConfiguration = protocolConfiguration;}

    public void add(final CoreSystems system, final URI uri)
    {
        systems.put(system, uri);
    }

    public URI get(final CoreSystems system)
    {
        return systems.get(system);
    }

    public boolean contains(final CoreSystems system)
    {
        return systems.containsKey(system);
    }

    public void addAll(final SystemEndpointHolder endpointHolder)
    {
        if (!Objects.equals(this.systems, endpointHolder.systems))
        { systems.putAll(endpointHolder.systems); }
    }

    public ProtocolConfiguration getProtocolConfiguration()
    {
        return protocolConfiguration;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("SystemEndpointHolder[");
        sb.append("systems=(").append(systems.size()).append(" entries)");
        sb.append(", protocolConfiguration=").append(protocolConfiguration);
        sb.append(']');
        return sb.toString();
    }
}
