package ch.chiodoni.lab.reactiveserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ratpack.func.Action;
import ratpack.guice.Guice;
import ratpack.launch.LaunchConfig;
import ratpack.launch.LaunchConfigBuilder;
import ratpack.server.RatpackServer;
import ratpack.server.RatpackServerBuilder;
import ratpack.stream.Streams;
import ratpack.websocket.*;
import ratpack.websocket.internal.WebsocketBroadcastSubscriber;
import reactor.rx.stream.HotStream;

import java.nio.file.FileSystems;

import static ratpack.websocket.WebSockets.websocket;
import static ratpack.websocket.WebSockets.websocketBroadcast;

public class Server {

    public final static boolean DEV = true;
    public final static int DEFAULT_PORT = 7070;
    public final static int THREADS = 1;
    public final static boolean TIME_RESPONSES = true;
    private final static Logger LOGGER = LoggerFactory.getLogger(Server.class);

    public static RatpackServer createServer(int port) {

        final HotStream<String> streamOfMouseMovements = reactor.rx.Streams.<String>defer();

//        streamOfMouseMovements.log().consume(s -> System.out.println("*** " + s));

        LaunchConfig config = LaunchConfigBuilder
            .baseDir(FileSystems.getDefault().getPath("src/ratpack/"))
            .indexFiles("index.html")
            .development(true)
            .timeResponses(TIME_RESPONSES)
            .threads(THREADS)
            .port(port)
            .development(DEV)
            .build(launchConfig -> Guice.builder(launchConfig)
                .bindings(bindingsSpec -> bindingsSpec.add(new EventsSourceModule()))
                .build(chain -> chain
                        .assets("public")
                        .handler("modulo/:modulo/:nr", ctx -> {
                                final int modulo = Integer.parseInt(ctx.getPathTokens().get("modulo"));
                                final int nr = Integer.parseInt(ctx.getPathTokens().get("nr"));
                                LOGGER.debug("Emit {} events modulo {}", nr, modulo);
                                websocketBroadcast(ctx, Streams.map(chain.getRegistry().get(EventsSource.class).events(modulo, nr), e -> Integer.toString(e.getNr())));
                            }
                        )
                        .handler("move", ctx ->
                                ctx.<Boolean>promise(f ->
                                        websocket(ctx, new WebSocketHandler<Object>() {
                                            @Override
                                            public Object onOpen(WebSocket webSocket) throws Exception {
                                                LOGGER.debug("Move socket is open");
                                                return null;
                                            }

                                            @Override
                                            public void onClose(WebSocketClose<Object> close) throws Exception {
                                                LOGGER.debug("Move socket is closed");
                                                f.success(true);
                                            }

                                            @Override
                                            public void onMessage(WebSocketMessage<Object> frame) throws Exception {
                                                LOGGER.debug("Move socket message {}", frame.getText());
                                                streamOfMouseMovements.accept(frame.getText());
                                            }
                                        })
                                ).then(Action.noop())
                        )
                        .handler("drawing", ctx ->
                                ctx.<Boolean>promise(f ->
                                        websocket(ctx, new AutoCloseWebSocketHandler<AutoCloseable>() {
                                            @Override
                                            public AutoCloseable onOpen(final WebSocket webSocket) throws Exception {
                                                WebsocketBroadcastSubscriber subscriber = new WebsocketBroadcastSubscriber(webSocket);
                                                streamOfMouseMovements.subscribe(subscriber);
                                                return subscriber;
                                            }

                                            @Override
                                            public void onClose(WebSocketClose<AutoCloseable> close) throws Exception {
                                                f.success(true);
                                                super.onClose(close);
                                            }
                                        })
                                ).then(Action.noop())
                        )

                ));
        return RatpackServerBuilder.build(config);
    }


    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            port = new Integer(args[0]);
        }
        createServer(port).start();
    }

}
