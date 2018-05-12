package org.apereo.cas.ticket.registry;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.logout.LogoutManager;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by anbug on 2018. 1. 8..
 */
public class CustomDefaultTicketRegistry extends AbstractMapBasedTicketRegistry {
    private final Map<String, Ticket> cache;
    private final Map<String, TicketGrantingTicket> grantingTicketCache;

    private final LogoutManager logoutManager;
    private final boolean single;

    public CustomDefaultTicketRegistry(int initialCapacity, int loadFactor, int concurrencyLevel, CipherExecutor cipherExecutor,
                                       final LogoutManager logoutManager, final boolean single) {
        super(cipherExecutor);
        this.cache = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        this.grantingTicketCache = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        this.logoutManager = logoutManager;
        this.single = single;
    }

    @Override
    public void addTicket(Ticket ticket) {
        if (this.single) {
            if (ticket instanceof TicketGrantingTicket) {
                TicketGrantingTicket grantingTicket = TicketGrantingTicket.class.cast(ticket);
                String username = grantingTicket.getAuthentication().getPrincipal().getId();
                Optional.ofNullable(this.grantingTicketCache.get(username))
                        .filter(t -> !t.getId().equals(grantingTicket.getId()))
                        .ifPresent(t -> {
                            super.deleteTicket(t.getId());
                            this.logoutManager.performLogout(t);
                        });
                this.grantingTicketCache.put(username, grantingTicket);
            }
        }

        super.addTicket(ticket);
    }

    @Override
    public Map<String, Ticket> getMapInstance() {
        return this.cache;
    }
}
