package com.joocy.synbad;

import io.netty.buffer.ByteBuf;
import io.reactivex.netty.protocol.http.server.HttpServerResponse;
import rx.Observable;

@FunctionalInterface
public interface State {

    Observable<Void> execute(HttpServerResponse<ByteBuf> httpResponse);

}
