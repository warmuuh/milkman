package milkman.sio.shaded.v2.client;

import milkman.sio.shaded.v2.client.Manager.Options;
import milkman.sio.shaded.v2.client.Manager.ReadyState;
import milkman.sio.shaded.v2.client.On.Handle;
import milkman.sio.shaded.v2.emitter.Emitter;
import milkman.sio.shaded.v2.parser.Packet;
import milkman.sio.shaded.v2.parser.Parser;
import milkman.sio.shaded.v2.thread.EventThread;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * monkey-patched onEvent to emit all events also to '*'
 */

/**
 * The socket class for Socket.IO Client.
 */
public class Socket extends Emitter {

    private static final Logger logger = Logger.getLogger(Socket.class.getName());

    /**
     * Called on a connection.
     */
    public static final String EVENT_CONNECT = "connect";

    /**
     * Called on a disconnection.
     */
    public static final String EVENT_DISCONNECT = "disconnect";

    /**
     * Called on a connection error.
     *
     * <p>Parameters:</p>
     * <ul>
     *   <li>(Exception) error data.</li>
     * </ul>
     */
    public static final String EVENT_CONNECT_ERROR = "connect_error";

    static final String EVENT_MESSAGE = "message";

    protected static Map<String, Integer> RESERVED_EVENTS = new HashMap<String, Integer>() {{
        put(EVENT_CONNECT, 1);
        put(EVENT_CONNECT_ERROR, 1);
        put(EVENT_DISCONNECT, 1);
        // used on the server-side
        put("disconnecting", 1);
        put("newListener", 1);
        put("removeListener", 1);
    }};

    /*package*/ String id;

    private volatile boolean connected;
    private int ids;
    private final String nsp;
    private final Manager io;
    private Map<String, String> auth;
    private final Map<Integer, Ack> acks = new HashMap<>();
    private Queue<Handle> subs;
    private final Queue<List<Object>> receiveBuffer = new LinkedList<>();
    private final Queue<Packet<JSONArray>> sendBuffer = new LinkedList<>();

    public Socket(Manager io, String nsp, Options opts) {
        this.io = io;
        this.nsp = nsp;
        if (opts != null) {
            this.auth = opts.auth;
        }
    }

    private void subEvents() {
        if (subs != null) return;

        Manager io = this.io;
        this.subs = new LinkedList<Handle>() {{
            add(On.on(io, Manager.EVENT_OPEN, new Listener() {
                @Override
                public void call(Object... args) {
                    onopen();
                }
            }));
            add(On.on(io, Manager.EVENT_PACKET, new Listener() {
                @Override
                public void call(Object... args) {
                    onpacket((Packet<?>) args[0]);
                }
            }));
            add(On.on(io, Manager.EVENT_ERROR, new Listener() {
                @Override
                public void call(Object... args) {
                    if (!connected) {
                        Socket.super.emit(EVENT_CONNECT_ERROR, args[0]);
                    }
                }
            }));
            add(On.on(io, Manager.EVENT_CLOSE, new Listener() {
                @Override
                public void call(Object... args) {
                    onclose(args.length > 0 ? (String) args[0] : null);
                }
            }));
        }};
    }

    public boolean isActive() {
        return subs != null;
    }

    /**
     * Connects the socket.
     */
    public Socket open() {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                if (connected || io.isReconnecting()) return;

                subEvents();
                io.open(); // ensure open
                if (ReadyState.OPEN == io.readyState) onopen();
            }
        });
        return this;
    }

    /**
     * Connects the socket.
     */
    public Socket connect() {
        return open();
    }

    /**
     * Send messages.
     *
     * @param args data to send.
     * @return a reference to this object.
     */
    public Socket send(Object... args) {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                emit(EVENT_MESSAGE, args);
            }
        });
        return this;
    }

    /**
     * Emits an event. When you pass {@link Ack} at the last argument, then the acknowledge is done.
     *
     * @param event an event name.
     * @param args data to send.
     * @return a reference to this object.
     */
    @Override
    public Emitter emit(String event, Object... args) {
        if (RESERVED_EVENTS.containsKey(event)) {
            throw new RuntimeException("'" + event + "' is a reserved event name");
        }

        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                Ack ack;
                Object[] _args;
                int lastIndex = args.length - 1;

                if (args.length > 0 && args[lastIndex] instanceof Ack) {
                    _args = new Object[lastIndex];
                    for (int i = 0; i < lastIndex; i++) {
                        _args[i] = args[i];
                    }
                    ack = (Ack) args[lastIndex];
                } else {
                    _args = args;
                    ack = null;
                }

                emit(event, _args, ack);
            }
        });
        return this;
    }

    /**
     * Emits an event with an acknowledge.
     *
     * @param event an event name
     * @param args data to send.
     * @param ack the acknowledgement to be called
     * @return a reference to this object.
     */
    public Emitter emit(String event, Object[] args, Ack ack) {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                JSONArray jsonArgs = new JSONArray();
                jsonArgs.put(event);

                if (args != null) {
                    for (Object arg : args) {
                        jsonArgs.put(arg);
                    }
                }

                Packet<JSONArray> packet = new Packet<>(Parser.EVENT, jsonArgs);

                if (ack != null) {
                    logger.fine(String.format("emitting packet with ack id %d", ids));
                    acks.put(ids, ack);
                    packet.id = ids++;
                }

                if (connected) {
                    packet(packet);
                } else {
                    sendBuffer.add(packet);
                }
            }
        });
        return this;
    }

    private void packet(Packet packet) {
        packet.nsp = nsp;
        io.packet(packet);
    }

    private void onopen() {
        logger.fine("transport is open - connecting");

        if (auth != null) {
            packet(new Packet<>(Parser.CONNECT, new JSONObject(auth)));
        } else {
            packet(new Packet<>(Parser.CONNECT));
        }
    }

    private void onclose(String reason) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("close (%s)", reason));
        }
        this.connected = false;
        this.id = null;
        super.emit(EVENT_DISCONNECT, reason);
    }

    private void onpacket(Packet<?> packet) {
        if (!nsp.equals(packet.nsp)) return;

        switch (packet.type) {
            case Parser.CONNECT:
                if (packet.data instanceof JSONObject && ((JSONObject) packet.data).has("sid")) {
                    try {
                        onconnect(((JSONObject) packet.data).getString("sid"));
                        return;
                    } catch (JSONException e) {}
                } else {
                    super.emit(EVENT_CONNECT_ERROR, new SocketIOException("It seems you are trying to reach a Socket.IO server in v2.x with a v3.x client, which is not possible"));
                }
                break;

            case Parser.EVENT: {
                @SuppressWarnings("unchecked")
                Packet<JSONArray> p = (Packet<JSONArray>) packet;
                onevent(p);
                break;
            }

            case Parser.BINARY_EVENT: {
                @SuppressWarnings("unchecked")
                Packet<JSONArray> p = (Packet<JSONArray>) packet;
                onevent(p);
                break;
            }

            case Parser.ACK: {
                @SuppressWarnings("unchecked")
                Packet<JSONArray> p = (Packet<JSONArray>) packet;
                onack(p);
                break;
            }

            case Parser.BINARY_ACK:
                @SuppressWarnings("unchecked")
                Packet<JSONArray> p = (Packet<JSONArray>) packet;
                onack(p);
                break;

            case Parser.DISCONNECT:
                ondisconnect();
                break;

            case Parser.CONNECT_ERROR:
                super.emit(EVENT_CONNECT_ERROR, packet.data);
                break;
        }
    }

    private void onevent(Packet<JSONArray> packet) {
        List<Object> args = new ArrayList<>(Arrays.asList(toArray(packet.data)));
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("emitting event %s", args));
        }

        if (packet.id >= 0) {
            logger.fine("attaching ack callback to event");
            args.add(ack(packet.id));
        }

        if (connected) {
            if (args.isEmpty()) return;
            String event = args.remove(0).toString();
            super.emit(event, args.toArray());
            args.add(0, event);
            super.emit("*", args.toArray());
        } else {
            receiveBuffer.add(args);
        }
    }

    private Ack ack(int id) {
        Socket self = this;
        boolean[] sent = {false};
        return new Ack() {
            @Override
            public void call(Object... args) {
                EventThread.exec(new Runnable() {
                    @Override
                    public void run() {
                        if (sent[0]) return;
                        sent[0] = true;
                        if (logger.isLoggable(Level.FINE)) {
                            logger.fine(String.format("sending ack %s", args.length != 0 ? args : null));
                        }

                        JSONArray jsonArgs = new JSONArray();
                        for (Object arg : args) {
                            jsonArgs.put(arg);
                        }

                        Packet<JSONArray> packet = new Packet<>(Parser.ACK, jsonArgs);
                        packet.id = id;
                        self.packet(packet);
                    }
                });
            }
        };
    }

    private void onack(Packet<JSONArray> packet) {
        Ack fn = acks.remove(packet.id);
        if (fn != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("calling ack %s with %s", packet.id, packet.data));
            }
            fn.call(toArray(packet.data));
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine(String.format("bad ack %s", packet.id));
            }
        }
    }

    private void onconnect(String id) {
        this.connected = true;
        this.id = id;
        emitBuffered();
        super.emit(EVENT_CONNECT);
    }

    private void emitBuffered() {
        List<Object> data;
        while ((data = receiveBuffer.poll()) != null) {
            String event = (String)data.get(0);
            super.emit(event, data.toArray());
        }
        receiveBuffer.clear();

        Packet<JSONArray> packet;
        while ((packet = sendBuffer.poll()) != null) {
            packet(packet);
        }
        sendBuffer.clear();
    }

    private void ondisconnect() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(String.format("server disconnect (%s)", nsp));
        }
        destroy();
        onclose("io server disconnect");
    }

    private void destroy() {
        if (subs != null) {
            // clean subscriptions to avoid reconnection
            for (Handle sub : subs) {
                sub.destroy();
            }
            this.subs = null;
        }

        io.destroy();
    }

    /**
     * Disconnects the socket.
     *
     * @return a reference to this object.
     */
    public Socket close() {
        EventThread.exec(new Runnable() {
            @Override
            public void run() {
                if (connected) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine(String.format("performing disconnect (%s)", nsp));
                    }
                    packet(new Packet(Parser.DISCONNECT));
                }

                destroy();

                if (connected) {
                    onclose("io client disconnect");
                }
            }
        });
        return this;
    }

    /**
     * Disconnects the socket.
     *
     * @return a reference to this object.
     */
    public Socket disconnect() {
        return close();
    }

    public Manager io() {
        return io;
    }

    public boolean connected() {
        return connected;
    }

    /**
     * A property on the socket instance that is equal to the underlying engine.io socket id.
     *
     * The value is present once the socket has connected, is removed when the socket disconnects and is updated if the socket reconnects.
     *
     * @return a socket id
     */
    public String id() {
        return id;
    }

    private static Object[] toArray(JSONArray array) {
        int length = array.length();
        Object[] data = new Object[length];
        for (int i = 0; i < length; i++) {
            Object v;
            try {
                v = array.get(i);
            } catch (JSONException e) {
                logger.log(Level.WARNING, "An error occured while retrieving data from JSONArray", e);
                v = null;
            }
            data[i] = JSONObject.NULL.equals(v) ? null : v;
        }
        return data;
    }
}

