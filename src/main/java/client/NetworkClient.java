package client;

import common.Protocol;
import org.json.JSONObject;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class NetworkClient {
    private static final String SERVER_HOST = "192.168.1.41";
    private static final int SERVER_PORT = 5555;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientGUI gui;
    private boolean connected;
    private Thread receiveThread;
    
    public NetworkClient(ClientGUI gui) {
        this.gui = gui;
        this.connected = false;
    }
    
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            
            // Start receiving thread
            receiveThread = new Thread(this::receiveMessages);
            receiveThread.start();
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void receiveMessages() {
        try {
            String message;
            while (connected && (message = in.readLine()) != null) {
                handleMessage(message);
            }
        } catch (IOException e) {
            if (connected) {
                System.out.println("Connection lost");
                disconnect();
            }
        }
    }
    
    private void handleMessage(String message) {
        try {
            JSONObject json = new JSONObject(message);
            String type = json.getString("type");
            
            switch (type) {
                case Protocol.LOGIN_SUCCESS:
                    gui.handleLoginSuccess(json);
                    break;
                case Protocol.LOGIN_FAILED:
                    gui.handleLoginFailed(json.getString("message"));
                    break;
                case Protocol.REGISTER_SUCCESS:
                    gui.handleRegisterSuccess(json.getString("message"));
                    break;
                case Protocol.REGISTER_FAILED:
                    gui.handleRegisterFailed(json.getString("message"));
                    break;
                case Protocol.PLAYER_LIST:
                    gui.updatePlayerList(json.getJSONArray("players"));
                    break;
                case Protocol.CHALLENGE_RECEIVED:
                    gui.handleChallengeReceived(json.getString("from"), 
                                               json.getString("difficulty"));
                    break;
                case Protocol.CHALLENGE_DECLINED:
                    gui.handleChallengeDeclined(json.getString("decliner"));
                    break;
                case Protocol.GAME_START:
                    gui.handleGameStart(json);
                    break;
                case Protocol.GAME_UPDATE:
                    gui.handleGameUpdate(json);
                    break;
                case Protocol.SCORE_UPDATE:
                    gui.handleScoreUpdate(json);
                    break;
                case Protocol.GAME_END:
                    gui.handleGameEnd(json);
                    break;
                case Protocol.OPPONENT_QUIT:
                    gui.handleOpponentQuit(json.getString("quitter"));
                    break;
                case Protocol.REMATCH_REQUEST:
                    gui.handleRematchRequest(json.getString("from"), 
                                            json.getString("difficulty"));
                    break;
                case Protocol.REMATCH_ACCEPTED:
                    gui.handleRematchAccepted(json.getString("from"));
                    break;
                case Protocol.REMATCH_DECLINED:
                    gui.handleRematchDeclined(json.getString("from"));
                    break;
                case Protocol.LEADERBOARD:
                    gui.updateLeaderboard(json.getJSONArray("data"));
                    break;
                case Protocol.ERROR:
                    gui.showError(json.getString("message"));
                    break;
                case Protocol.OPPONENT_LEFT_LOBBY:
                    gui.handleOpponentLeftLobby(message);
                    break;
                case Protocol.MATCH_HISTORY:
                    gui.updateMatchHistory(json.getJSONArray("data"));
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void getMatchHistory() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.GET_MATCH_HISTORY);
        sendMessage(msg);
    }
    
    public void sendMessage(Map<String, Object> data) {
        if (connected && out != null) {
            JSONObject json = new JSONObject(data);
            out.println(json.toString());
        }
    }
    
    public void login(String username, String password) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.LOGIN);
        msg.put("username", username);
        msg.put("password", password);
        sendMessage(msg);
    }
    
    public void register(String username, String password) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.REGISTER);
        msg.put("username", username);
        msg.put("password", password);
        sendMessage(msg);
    }
    
    public void getPlayers() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.GET_PLAYERS);
        sendMessage(msg);
    }
    
    public void sendChallenge(String target, String difficulty) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.CHALLENGE);
        msg.put("target", target);
        msg.put("difficulty", difficulty);
        sendMessage(msg);
    }
    
    public void acceptChallenge(String challenger, String difficulty) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.ACCEPT_CHALLENGE);
        msg.put("challenger", challenger);
        msg.put("difficulty", difficulty);
        sendMessage(msg);
    }
    
    public void declineChallenge(String challenger) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.DECLINE_CHALLENGE);
        msg.put("challenger", challenger);
        sendMessage(msg);
    }
    
    public void flipCards(int card1, int card2) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.CARD_FLIP);
        msg.put("card1", card1);
        msg.put("card2", card2);
        sendMessage(msg);
    }
    
    public void quitGame() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.QUIT_GAME);
        sendMessage(msg);
    }
    
    public void sendRematch(String target, String difficulty, boolean accept) {
        Map<String, Object> msg = new HashMap<>();
        
        if (accept) {
            // Check if this is initial request or accepting
            msg.put("type", Protocol.REMATCH);
            msg.put("target", target);
            msg.put("difficulty", difficulty);
            msg.put("accept", true);
        } else {
            // Declining
            msg.put("type", Protocol.REMATCH);
            msg.put("target", target);
            msg.put("difficulty", difficulty);
            msg.put("accept", false);
        }
        
        sendMessage(msg);
    }
    
    public void sendRematchRequest(String target, String difficulty) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.REMATCH);
        msg.put("target", target);
        msg.put("difficulty", difficulty);
        msg.put("isRequest", true); // Flag to indicate this is initial request
        sendMessage(msg);
    }
    
    public void getLeaderboard() {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.GET_LEADERBOARD);
        sendMessage(msg);
    }
    
    public void disconnect() {
        connected = false;
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("type", Protocol.LOGOUT);
            sendMessage(msg);
            
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void notifyLeftLobby(String opponentName) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", Protocol.OPPONENT_LEFT_LOBBY);
        msg.put("opponent", opponentName);
        sendMessage(msg);
    }

    public boolean isConnected() {
        return connected;
    }
}