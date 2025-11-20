package server;

import common.Protocol;
import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Server server;
    private int playerId;
    private String username;
    private boolean isLoggedIn;
    private boolean inGame;
    private GameRoom currentGame;
    
    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        this.isLoggedIn = false;
        this.inGame = false;
        
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void run() {
        try {
            String message;
            while ((message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + username);
        } finally {
            disconnect();
        }
    }
    
    private void handleMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");
            
            switch (type) {
                case Protocol.LOGIN:
                    handleLogin(json);
                    break;
                case Protocol.REGISTER:
                    handleRegister(json);
                    break;
                case Protocol.GET_PLAYERS:
                    handleGetPlayers();
                    break;
                case Protocol.CHALLENGE:
                    handleChallenge(json);
                    break;
                case Protocol.ACCEPT_CHALLENGE:
                    handleAcceptChallenge(json);
                    break;
                case Protocol.DECLINE_CHALLENGE:
                    handleDeclineChallenge(json);
                    break;
                case Protocol.CARD_FLIP:
                    handleCardFlip(json);
                    break;
                case Protocol.QUIT_GAME:
                    handleQuitGame();
                    break;
                case Protocol.REMATCH:
                    handleRematch(json);
                    break;
                case Protocol.GET_LEADERBOARD:
                    handleGetLeaderboard();
                    break;
                case Protocol.LOGOUT:
                    disconnect();
                    break;
                case Protocol.OPPONENT_LEFT_LOBBY:
                    handleLeftLobby(json);
                    break;
                case Protocol.GET_MATCH_HISTORY:
                    handleGetMatchHistory();
                    break;   
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError("Invalid message format");
        }
    }
    
    private void handleGetMatchHistory() {
        if (!isLoggedIn) {
            sendError("Please login first");
            return;
        }

        DatabaseManager db = Server.getDatabase();
        List<Map<String, Object>> history = db.getPlayerMatchHistory(this.playerId, 20);

        Map<String, Object> response = new HashMap<>();
        response.put("type", Protocol.MATCH_HISTORY);
        response.put("data", history);
        sendMessage(response);

        System.out.println("📜 Sent match history to " + username + " (" + history.size() + " matches)");
    }
    
    private void handleLeftLobby(JSONObject json) {
        String opponentName = json.getString("opponent");
        ClientHandler opponent = server.findPlayerByUsername(opponentName);

        if (opponent != null) {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "OPPONENT_LEFT_LOBBY");
            notification.put("opponent", this.username);
            opponent.sendMessage(notification);

            System.out.println("📤 " + this.username + " left lobby, notified " + opponentName);
        }
    }
    
    private void handleLogin(JSONObject json) {
        String user = json.getString("username");
        String pass = json.getString("password");
        
        DatabaseManager db = Server.getDatabase();
        Map<String, Object> playerData = db.loginPlayer(user, pass);
        
        if (playerData != null) {
            this.playerId = (int) playerData.get("id");
            this.username = user;
            this.isLoggedIn = true;
            
            Map<String, Object> response = new HashMap<>(playerData);
            response.put("type", Protocol.LOGIN_SUCCESS);
            sendMessage(response);
            
            server.addOnlinePlayer(this);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("type", Protocol.LOGIN_FAILED);
            response.put("message", "Invalid credentials or account banned");
            sendMessage(response);
        }
    }
    
    private void handleRegister(JSONObject json) {
        String user = json.getString("username");
        String pass = json.getString("password");
        
        DatabaseManager db = Server.getDatabase();
        boolean success = db.registerPlayer(user, pass);
        
        Map<String, Object> response = new HashMap<>();
        if (success) {
            response.put("type", Protocol.REGISTER_SUCCESS);
            response.put("message", "Registration successful");
        } else {
            response.put("type", Protocol.REGISTER_FAILED);
            response.put("message", "Username already exists");
        }
        sendMessage(response);
    }
    
    private void handleGetPlayers() {
        List<Map<String, Object>> players = server.getOnlinePlayers();
        Map<String, Object> response = new HashMap<>();
        response.put("type", Protocol.PLAYER_LIST);
        response.put("players", players);
        sendMessage(response);
    }
    
    private void handleChallenge(JSONObject json) {
        String targetUsername = json.getString("target");
        String difficulty = json.getString("difficulty");
        
        ClientHandler target = server.findPlayerByUsername(targetUsername);
        if (target != null && !target.isInGame()) {
            Map<String, Object> challenge = new HashMap<>();
            challenge.put("type", Protocol.CHALLENGE_RECEIVED);
            challenge.put("from", this.username);
            challenge.put("difficulty", difficulty);
            target.sendMessage(challenge);
        } else {
            sendError("Player not available");
        }
    }
    
    private void handleAcceptChallenge(JSONObject json) {
        String challengerUsername = json.getString("challenger");
        String difficulty = json.getString("difficulty");
        
        ClientHandler challenger = server.findPlayerByUsername(challengerUsername);
        if (challenger != null && !challenger.isInGame() && !this.isInGame()) {
            // Tạo game room
            String roomId = UUID.randomUUID().toString();
            GameRoom room = new GameRoom(roomId, challenger, this, difficulty);
            
            this.currentGame = room;
            challenger.currentGame = room;
            this.inGame = true;
            challenger.inGame = true;
            
            server.addGameRoom(room);
        } else {
            sendError("Cannot start game");
        }
    }
    
    private void handleDeclineChallenge(JSONObject json) {
        String challengerUsername = json.getString("challenger");
        ClientHandler challenger = server.findPlayerByUsername(challengerUsername);
        
        if (challenger != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("type", Protocol.CHALLENGE_DECLINED);
            response.put("decliner", this.username);
            challenger.sendMessage(response);
        }
    }
    
    private void handleCardFlip(JSONObject json) {
        if (currentGame != null && inGame) {
            int card1 = json.getInt("card1");
            int card2 = json.getInt("card2");
            currentGame.flipCard(this, card1, card2);
        }
    }
    
    private void handleQuitGame() {
        if (currentGame != null && inGame) {
            currentGame.playerQuit(this);
            currentGame = null;
            inGame = false;
        }
    }
    
    private void handleRematch(JSONObject json) {
        String targetUsername = json.getString("target");
        String difficulty = json.getString("difficulty");
        
        ClientHandler target = server.findPlayerByUsername(targetUsername);
        
        if (target == null) {
            sendError("Player not found");
            return;
        }
        
        // Check if this is initial request or response
        boolean isRequest = json.optBoolean("isRequest", false);
        
        if (isRequest) {
            // Send rematch request to target
            Map<String, Object> requestMsg = new HashMap<>();
            requestMsg.put("type", Protocol.REMATCH_REQUEST);
            requestMsg.put("from", this.username);
            requestMsg.put("difficulty", difficulty);
            target.sendMessage(requestMsg);
            
            System.out.println("📬 Rematch request: " + this.username + " → " + targetUsername + " (" + difficulty + ")");
        } else {
            // This is a response (accept/decline)
            boolean accept = json.getBoolean("accept");
            
            if (accept) {
                // Check if both players are available
                if (!target.isInGame() && !this.isInGame()) {
                    // Notify the requester
                    Map<String, Object> response = new HashMap<>();
                    response.put("type", Protocol.REMATCH_ACCEPTED);
                    response.put("from", this.username);
                    target.sendMessage(response);
                    
                    // Create new game room
                    String roomId = UUID.randomUUID().toString();
                    GameRoom room = new GameRoom(roomId, target, this, difficulty);
                    this.currentGame = room;
                    target.currentGame = room;
                    this.inGame = true;
                    target.inGame = true;
                    server.addGameRoom(room);
                    
                    System.out.println("✓ Rematch accepted - New game created");
                } else {
                    sendError("Cannot start rematch - player busy");
                }
            } else {
                // Declined
                Map<String, Object> response = new HashMap<>();
                response.put("type", Protocol.REMATCH_DECLINED);
                response.put("from", this.username);
                target.sendMessage(response);
                
                System.out.println("✗ Rematch declined: " + this.username);
            }
        }
    }
    
    private void handleGetLeaderboard() {
        DatabaseManager db = Server.getDatabase();
        List<Map<String, Object>> leaderboard = db.getLeaderboard(10);
        
        Map<String, Object> response = new HashMap<>();
        response.put("type", Protocol.LEADERBOARD);
        response.put("data", leaderboard);
        sendMessage(response);
    }
    
    public void sendMessage(Map<String, Object> data) {
        try {
            JSONObject json = new JSONObject(data);
            out.println(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendError(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("type", Protocol.ERROR);
        error.put("message", message);
        sendMessage(error);
    }
    
    private void disconnect() {
        if (inGame && currentGame != null) {
            currentGame.playerQuit(this);
        }
        
        if (isLoggedIn) {
            server.removeOnlinePlayer(this);
        }
        
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getPlayerId() {
        return playerId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isInGame() {
        return inGame;
    }
    
    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
}