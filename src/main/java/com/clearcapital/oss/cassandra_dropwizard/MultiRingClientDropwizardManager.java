package com.clearcapital.oss.cassandra_dropwizard;

import com.clearcapital.oss.cassandra.configuration.MultiRingConfiguration;
import com.clearcapital.oss.cassandra.multiring.MultiRingClientManager;

import io.dropwizard.lifecycle.Managed;

public class MultiRingClientDropwizardManager implements Managed {

    MultiRingClientManager clientManager;
    MultiRingConfiguration configuration;

    public MultiRingClientDropwizardManager(MultiRingConfiguration configuration) throws Exception {
        this.configuration = configuration;
        start();
    }

    @Override
    public void start() throws Exception {
        clientManager = new MultiRingClientManager(configuration);
    }

    @Override
    public void stop() throws Exception {
        clientManager.disconnectAll();
        clientManager = null;
    }

    public MultiRingClientManager getClientManager() {
        return clientManager;
    }

}
