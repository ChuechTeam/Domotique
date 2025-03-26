package fr.domotique.base;

import fr.domotique.api.users.*;
import io.vertx.core.buffer.*;
import io.vertx.core.eventbus.*;

/// Just a dumb codec so we can send [event bus][EventBus] messages locally.
public final class LocalCodec<T> implements MessageCodec<T, Object> {
    @Override
    public void encodeToWire(Buffer buffer, T msg) {
        throw new UnsupportedOperationException("Not supported by LocalCodec.");
    }

    @Override
    public Object decodeFromWire(int pos, Buffer buffer) {
        throw new UnsupportedOperationException("Not supported by LocalCodec.");
    }

    @Override
    public Object transform(T msg) {
        return msg;
    }

    @Override
    public String name() {
        return "LocalCodec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
