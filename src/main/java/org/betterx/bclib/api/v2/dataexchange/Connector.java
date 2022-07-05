package org.betterx.bclib.api.v2.dataexchange;

import org.betterx.bclib.api.v2.dataexchange.handler.DataExchange;

import java.util.Set;

abstract class Connector {
    protected final DataExchange api;

    Connector(DataExchange api) {
        this.api = api;
    }

    public abstract boolean onClient();

    protected Set<DataHandlerDescriptor> getDescriptors() {
        return api.getDescriptors();
    }
}
