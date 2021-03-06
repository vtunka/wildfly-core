/*
* JBoss, Home of Professional Open Source
* Copyright 2010, Red Hat Inc., and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.network;

import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Managed {@code DatagramSocket} binding, automatically registering itself
 * at the {@code SocketBindingManager} when bound.
 *
 * @author Emanuel Muckenhuber
 */
public class ManagedDatagramSocketBinding extends DatagramSocket implements ManagedBinding {

    private final String name;
    private final SocketAddress address;
    private final ManagedBindingRegistry registry;

    ManagedDatagramSocketBinding(final String name, final ManagedBindingRegistry socketBindings, SocketAddress address) throws SocketException {
        super(address);
        this.name = name;
        this.address = address;
        this.registry = socketBindings;
        if (this.isBound()) {
            this.registry.registerBinding(this);
        }
    }

    @Override
    public String getSocketBindingName() {
        return name;
    }

    @Override
    public InetSocketAddress getBindAddress() {
        if (name == null) {
            // unnamed datagram socket
            return (InetSocketAddress) address;
        } else {
            return (InetSocketAddress) getLocalSocketAddress();
        }
    }

    @Override
    public synchronized void bind(SocketAddress addr) throws SocketException {
        super.bind(addr);
        // This method might have been called from the super constructor
        if (this.registry != null) {
            this.registry.registerBinding(this);
        }
    }

    @Override
    public void close() {
        // First unregister, then close. This allows UnnamedRegistryImpl
        // to get the bind address before it's gone
        try {
            // This method might have been called from the super constructor
            if (this.registry != null) {
                registry.unregisterBinding(this);
            }
        } finally {
            super.close();
        }
    }
}

