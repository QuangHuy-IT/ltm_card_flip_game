package server;

import common.Protocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static final int PORT = 5555;
    private static DatabaseManager database;
    private Map<String, ClientHandler> onlinePlayers;
    private Map<String, GameRoom> gameRooms;
    private ServerSocket serverSocket;
    private boolean running;
    
    public Server() {
        database = new DatabaseManager();
        onlinePlayers = new ConcurrentHashMap<>();
        gameRooms = new ConcurrentHashMap<>();
        running = true;
        
        // Verify database integrity on startup
        database.verifyDatabaseIntegrity();
    }
    
    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);
            
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                
                ClientHandler handler = new ClientHandler(clientSocket, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void addOnlinePlayer(ClientHandler player) {
        onlinePlayers.put(player.getUsername(), player);
        System.out.println("Player online: " + player.getUsername());
        broadcastPlayerList();
    }
    
    public synchronized void removeOnlinePlayer(ClientHandler player) {
        onlinePlayers.remove(player.getUsername());
        System.out.println("Player offline: " + player.getUsername());
        broadcastPlayerList();
    }
    
    public List<Map<String, Object>> getOnlinePlayers() {
        List<Map<String, Object>> players = new ArrayList<>();
        for (ClientHandler player : onlinePlayers.values()) {
            Map<String, Object> playerInfo = new HashMap<>();
            playerInfo.put("username", player.getUsername());
            playerInfo.put("inGame", player.isInGame());
            players.add(playerInfo);
        }
        return players;
    }
    
    private void broadcastPlayerList() {
        List<Map<String, Object>> players = getOnlinePlayers();
        Map<String, Object> message = new HashMap<>();
        message.put("type", Protocol.PLAYER_LIST);
        message.put("players", players);
        
        for (ClientHandler player : onlinePlayers.values()) {
            player.sendMessage(message);
        }
    }
    
    public ClientHandler findPlayerByUsername(String username) {
        return onlinePlayers.get(username);
    }
    
    public void addGameRoom(GameRoom room) {
        gameRooms.put(room.getRoomId(), room);
        System.out.println("Game room created: " + room.getRoomId());
    }
    
    public void removeGameRoom(String roomId) {
        gameRooms.remove(roomId);
        System.out.println("Game room closed: " + roomId);
    }
    
    public static DatabaseManager getDatabase() {
        return database;
    }
    
    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            database.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) throws UnknownHostException {
        Server server = new Server();
        
        // Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            server.stop();
        }));
        
        InetAddress ip = InetAddress.getLocalHost();
        System.out.println("Server IP: " + ip.getHostAddress());
        
        server.start();
    }
}