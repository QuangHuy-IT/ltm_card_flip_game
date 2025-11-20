package server;

import common.Protocol;
import java.util.*;

public class GameRoom {
    private String roomId;
    private ClientHandler player1;
    private ClientHandler player2;
    private String difficulty;
    private int cardCount;
    private int timeLimit;
    
    // Mỗi player có bàn riêng
    private int[] player1CardValues;
    private int[] player2CardValues;
    private boolean[] player1FlippedCards;
    private boolean[] player2FlippedCards;
    
    private int player1Score;
    private int player2Score;
    private int player1MatchedPairs;
    private int player2MatchedPairs;
    
    private long startTime;
    private Timer gameTimer;
    private boolean gameEnded;
    
    public int gameRows;
    public int gameCols;
    
    public GameRoom(String roomId, ClientHandler p1, ClientHandler p2, String difficulty) {
        this.roomId = roomId;
        this.player1 = p1;
        this.player2 = p2;
        this.difficulty = difficulty;
        this.cardCount = Protocol.getCardCount(difficulty);
        this.timeLimit = Protocol.getTimeLimit(difficulty);
        this.player1Score = 0;
        this.player2Score = 0;
        this.player1MatchedPairs = 0;
        this.player2MatchedPairs = 0;
        this.gameEnded = false;
        
        // Khởi tạo 2 bàn độc lập
        initializePlayerCards();
        startGame();
    }
    
    private void initializePlayerCards() {
        int pairs = cardCount / 2;

        // Tạo 1 bộ bài duy nhất cho cả 2 người chơi
        List<Integer> sharedValues = new ArrayList<>();
        for (int i = 0; i < pairs; i++) {
            sharedValues.add(i);
            sharedValues.add(i);
        }
        Collections.shuffle(sharedValues); // Xáo bài 1 lần duy nhất

        // Cả 2 player dùng chung bộ bài này
        player1CardValues = new int[cardCount];
        player2CardValues = new int[cardCount];
        for (int i = 0; i < cardCount; i++) {
            player1CardValues[i] = sharedValues.get(i);
            player2CardValues[i] = sharedValues.get(i); // Giống hệt player 1
        }

        // Khởi tạo trạng thái lật bài riêng
        player1FlippedCards = new boolean[cardCount];
        player2FlippedCards = new boolean[cardCount];
        Arrays.fill(player1FlippedCards, false);
        Arrays.fill(player2FlippedCards, false);
        
        int cols = (int) Math.ceil(Math.sqrt(cardCount));
        int rows = (int) Math.ceil((double) cardCount / cols);
        
        gameRows = rows;
        gameCols = cols;

            
        System.out.println("✓ Initialized IDENTICAL game boards for both players with " + cardCount + " cards");
    }
    
    private void startGame() {
        startTime = System.currentTimeMillis();
        
        // Chuyển cardValues thành List
        List<Integer> cardValuesList1 = new ArrayList<>();
        for (int value : player1CardValues) {
            cardValuesList1.add(value);
        }
        
        List<Integer> cardValuesList2 = new ArrayList<>();
        for (int value : player2CardValues) {
            cardValuesList2.add(value);
        }
        
        // Gửi cho player 1 - bàn riêng của họ
        Map<String, Object> gameData1 = new HashMap<>();
        gameData1.put("type", Protocol.GAME_START);
        gameData1.put("roomId", roomId);
        gameData1.put("difficulty", difficulty);
        gameData1.put("cardCount", cardCount);
        gameData1.put("timeLimit", timeLimit);
        gameData1.put("opponent", player2.getUsername());
        gameData1.put("cardValues", cardValuesList1);
        gameData1.put("rows", gameRows);
        gameData1.put("cols", gameCols);
        player1.sendMessage(gameData1);
        
        // Gửi cho player 2 - bàn riêng của họ
        Map<String, Object> gameData2 = new HashMap<>();
        gameData2.put("type", Protocol.GAME_START);
        gameData2.put("roomId", roomId);
        gameData2.put("difficulty", difficulty);
        gameData2.put("cardCount", cardCount);
        gameData2.put("timeLimit", timeLimit);
        gameData2.put("opponent", player1.getUsername());
        gameData2.put("cardValues", cardValuesList2);
        gameData2.put("rows", gameRows);
        gameData2.put("cols", gameCols);
        player2.sendMessage(gameData2);
        
        System.out.println("🎮 Game started - " + player1.getUsername() + " vs " + player2.getUsername());
        
        // Bắt đầu timer
        gameTimer = new Timer();
        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                endGameByTime();
            }
        }, timeLimit * 1000L);
    }
    
    public synchronized boolean flipCard(ClientHandler player, int card1, int card2) {
        if (gameEnded) return false;
        if (card1 < 0 || card1 >= cardCount || card2 < 0 || card2 >= cardCount) return false;
        if (card1 == card2) return false;
        
        boolean isPlayer1 = (player == player1);
        int[] cardValues = isPlayer1 ? player1CardValues : player2CardValues;
        boolean[] flippedCards = isPlayer1 ? player1FlippedCards : player2FlippedCards;
        
        if (flippedCards[card1] || flippedCards[card2]) return false;
        
        boolean matched = cardValues[card1] == cardValues[card2];
        
        Map<String, Object> updateMsg = new HashMap<>();
        updateMsg.put("type", Protocol.GAME_UPDATE);
        updateMsg.put("player", player.getUsername());
        updateMsg.put("card1", card1);
        updateMsg.put("card2", card2);
        updateMsg.put("value1", cardValues[card1]);
        updateMsg.put("value2", cardValues[card2]);
        updateMsg.put("matched", matched);
        
        if (matched) {
            flippedCards[card1] = true;
            flippedCards[card2] = true;
            
            if (isPlayer1) {
                player1Score += 10;
                player1MatchedPairs++;
            } else {
                player2Score += 10;
                player2MatchedPairs++;
            }
            
            // Gửi update cho chính người chơi
            player.sendMessage(updateMsg);
            
            // Gửi update điểm cho cả 2
            broadcastScores();
            
            // Kiểm tra xem player này đã lật hết chưa
            if (isPlayer1 && player1MatchedPairs >= cardCount / 2) {
                endGameByCompletion(player1);
            } else if (!isPlayer1 && player2MatchedPairs >= cardCount / 2) {
                endGameByCompletion(player2);
            }
        } else {
            // Chỉ gửi cho người chơi đó thôi
            player.sendMessage(updateMsg);
        }
        
        return matched;
    }
    
    private void broadcastScores() {
        Map<String, Object> scoreMsg = new HashMap<>();
        scoreMsg.put("type", Protocol.SCORE_UPDATE);
        scoreMsg.put(player1.getUsername(), player1Score);
        scoreMsg.put(player2.getUsername(), player2Score);
        
        player1.sendMessage(scoreMsg);
        player2.sendMessage(scoreMsg);
    }
    
    private void endGameByCompletion(ClientHandler winner) {
        if (gameEnded) return;
        endGame(winner.getUsername());
    }
    
    private void endGameByTime() {
        if (!gameEnded) {
            // Ai có điểm cao hơn thắng
            String winner;
            if (player1Score > player2Score) {
                winner = player1.getUsername();
            } else if (player2Score > player1Score) {
                winner = player2.getUsername();
            } else {
                winner = "DRAW";
            }
            endGame(winner);
        }
    }
    
    private void endGame(String winnerName) {
        gameEnded = true;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        long duration = (System.currentTimeMillis() - startTime) / 1000;
        ClientHandler winnerHandler = null;
        ClientHandler loserHandler = null;
        
        if (!winnerName.equals("DRAW")) {
            if (winnerName.equals(player1.getUsername())) {
                winnerHandler = player1;
                loserHandler = player2;
            } else {
                winnerHandler = player2;
                loserHandler = player1;
            }
        }
        
        // Gửi kết quả
        Map<String, Object> endMsg = new HashMap<>();
        endMsg.put("type", Protocol.GAME_END);
        endMsg.put("winner", winnerName);
        endMsg.put("player1", player1.getUsername());
        endMsg.put("player2", player2.getUsername());
        endMsg.put("player1Score", player1Score);
        endMsg.put("player2Score", player2Score);
        endMsg.put("duration", duration);
        
        player1.sendMessage(endMsg);
        player2.sendMessage(endMsg);
        
        // Cập nhật database
        DatabaseManager db = Server.getDatabase();
        if (winnerHandler != null) {
            db.updatePlayerScore(winnerHandler.getPlayerId(), 
                                winnerHandler == player1 ? player1Score : player2Score, true);
            db.updatePlayerScore(loserHandler.getPlayerId(), 
                                loserHandler == player1 ? player1Score : player2Score, false);
            db.saveMatch(player1.getPlayerId(), player2.getPlayerId(), 
                        winnerHandler.getPlayerId(), difficulty, 
                        player1Score, player2Score, (int)duration);
        } else {
            // Draw
            db.updatePlayerScore(player1.getPlayerId(), player1Score, false);
            db.updatePlayerScore(player2.getPlayerId(), player2Score, false);
            db.saveMatch(player1.getPlayerId(), player2.getPlayerId(), 
                        -1, difficulty, player1Score, player2Score, (int)duration);
        }
        
        System.out.println("🏁 Game ended - Winner: " + winnerName + " | " + 
                          player1.getUsername() + ": " + player1Score + " | " + 
                          player2.getUsername() + ": " + player2Score);
        
        player1.setInGame(false);
        player2.setInGame(false);
    }
    
    public void playerQuit(ClientHandler player) {
        if (gameEnded) return;
        
        gameEnded = true;
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        ClientHandler opponent = (player == player1) ? player2 : player1;
        
        Map<String, Object> quitMsg = new HashMap<>();
        quitMsg.put("type", Protocol.OPPONENT_QUIT);
        quitMsg.put("quitter", player.getUsername());
        opponent.sendMessage(quitMsg);
        
        // Cập nhật quit count
        DatabaseManager db = Server.getDatabase();
        db.incrementQuitCount(player.getPlayerId());
        
        // Opponent thắng
        int opponentScore = (opponent == player1) ? player1Score : player2Score;
        db.updatePlayerScore(opponent.getPlayerId(), Math.max(opponentScore, 100), true);
        db.updatePlayerScore(player.getPlayerId(), 0, false);
        
        System.out.println("⚠ " + player.getUsername() + " quit the game");
        
        player.setInGame(false);
        opponent.setInGame(false);
    }
    
    public String getRoomId() {
        return roomId;
    }
    
    public ClientHandler getPlayer1() {
        return player1;
    }
    
    public ClientHandler getPlayer2() {
        return player2;
    }
}