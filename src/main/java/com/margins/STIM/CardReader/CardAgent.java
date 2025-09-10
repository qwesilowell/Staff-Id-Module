package com.margins.STIM.CardReader;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.glassfish.tyrus.server.Server;

import javax.smartcardio.*;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@ServerEndpoint("/card")
public class CardAgent {

    private static Set<Session> sessions = new CopyOnWriteArraySet<>();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        System.out.println("Client connected: " + session.getId());
        session.getAsyncRemote().sendText("{\"debug\":\"Connected to server\"}");
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received from client: " + message);
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        System.out.println("Client disconnected: " + session.getId());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    public static void broadcast(String msg) {
        for (Session s : sessions) {
            if (s.isOpen()) {
                s.getAsyncRemote().sendText(msg);
            }
        }
        System.out.println("Broadcast: " + msg);
    }

    // =========================
    // Smartcard Reader Thread
    // =========================
    private static void startCardListener() {
        new Thread(() -> {
            try {
                TerminalFactory factory = TerminalFactory.getDefault();
                List<CardTerminal> terminals = factory.terminals().list();

                for (CardTerminal terminal : terminals) {
                    System.out.println("Using terminal: " + terminal.getName());
                }
                for (CardTerminal t : terminals) {
                    System.out.println("Terminal: " + t.getName() + " present=" + t.isCardPresent());
                }

                if (terminals.isEmpty()) {
                    System.err.println("No card terminals found!");
                    return;
                }

                CardTerminal terminal = terminals.get(0); // pick first for now

                while (true) {
                    terminal.waitForCardPresent(0);
                    try {
                        Card card = terminal.connect("*");
                        CardChannel channel = card.getBasicChannel();

                        byte[] response;

                        if (terminal.getName().contains("ICC 0")) {
                            // Contact card (insert)
                            CommandAPDU selectMF = new CommandAPDU(
                                    new byte[]{0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3F, 0x00});
                            response = channel.transmit(selectMF).getBytes();
                            System.out.println("Contact card response: " + bytesToHex(response));
                            broadcast("{\"cardType\":\"contact\",\"response\":\"" + bytesToHex(response) + "\"}");

                        }  else if (terminal.getName().contains("PICC 0")) {
                            // Contactless card (tap) – Get UID
                            CommandAPDU getUID = new CommandAPDU(
                                    new byte[]{(byte) 0xFF, (byte) 0xCA, 0x00, 0x00, 0x00});
                            response = channel.transmit(getUID).getBytes();
                            System.out.println("Contactless card UID: " + bytesToHex(response));
                            broadcast("{\"cardType\":\"contactless\",\"uid\":\"" + bytesToHex(response) + "\"}");

                        } else {
                            System.out.println("Unknown terminal type: " + terminal.getName());
                        }

                        card.disconnect(false);

                    } catch (Exception e) {
                        System.err.println("Card read error: " + e);
                    }

                    terminal.waitForCardAbsent(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    // =========================
    // Main entry point
    // =========================
    public static void main(String[] args) {
        Server server = new Server("localhost", 8081, "/", null, CardAgent.class);
        try {
            server.start();
            System.out.println("CardAgent running at ws://localhost:8081/card");

            // Start smartcard reader loop
            startCardListener();

            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
