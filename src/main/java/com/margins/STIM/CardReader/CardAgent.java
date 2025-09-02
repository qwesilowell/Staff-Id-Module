//package com.margins.STIM.CardReader;
//
//import jakarta.websocket.*;
//import jakarta.websocket.server.ServerEndpoint;
//import org.glassfish.tyrus.server.Server;
//import org.json.JSONObject;
//import com.margins.STIM.util.MyReadCardUtil;
//import com.c10n.scalibur.smartcards.SmartCardTerminal;
//
///**
// *
// * @author PhilipManteAsare
// */
//@ServerEndpoint("/card")
//public class CardAgent {
//
//    private static MyReadCardUtil cardUtil = new MyReadCardUtil();
//
//    @OnOpen
//    public void onOpen(Session session) {
//        System.out.println("Browser connected: " + session.getId());
//    }
//    
//    @OnMessage
//    public String onMessage(String message, Session session) {
//        try {
//            System.out.println("Received: " + message);
//
//            JSONObject req = new JSONObject(message);
//            String action = req.optString("action", "");
//            String can = req.optString("can", "");
//
//            if ("readCard".equalsIgnoreCase(action)) {
//                if (can.isEmpty()) {
//                    return new JSONObject().put("error", "CAN is required").toString();
//                }
//
//                SmartCardTerminal terminal = cardUtil.loadSmartReader();
//                if (terminal == null) {
//                    return new JSONObject().put("error", "No card reader found.").toString();
//                }
//
//                JSONObject cardData = cardUtil.readCardDetailAsJSON(terminal, can);
//                if (cardData == null) {
//                    return new JSONObject().put("error", "Failed to read card").toString();
//                }
//
//                return cardData.toString();
//            } else if ("debugReaders".equalsIgnoreCase(action)) {
//                String readersInfo = cardUtil.debugReaders();  // to get connected device
//                return new JSONObject()
//                        .put("debug", readersInfo)
//                        .toString();
//            }
//
//            return new JSONObject().put("error", "Unknown action").toString();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new JSONObject().put("error", e.getMessage()).toString();
//        }
//    }
//
//    @OnClose
//    public void onClose(Session session) {
//        System.out.println("Browser disconnected: " + session.getId());
//    }
//
//    public static void main(String[] args) {
//        Server server = new Server("localhost", 8081, "/", null, CardAgent.class);
//        try {
//            server.start();
//            System.out.println("Card Agent running at ws://localhost:8081/card");
//            Thread.currentThread().join(); // Keep alive
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
