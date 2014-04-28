package com.joocy.synbad;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

public class Machine {

    private State[] states;
    private int currentState = 0;

    public Machine(final State[] states) {
        this.states = states;
    }

    public Observable<Void> execute(HttpServerResponse<ByteBuf> httpServerResponse) {
        State state = states[currentState];
        incrementCurrentState();
        return state.execute(httpServerResponse);
    }

    private void incrementCurrentState() {
        currentState++;
        if (currentState >= states.length) currentState = 0;
    }

}
