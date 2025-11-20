package common;

/**
 * Protocol định nghĩa các loại message giữa Client và Server
 */
public class Protocol {
    // Client -> Server
    public static final String LOGIN = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String LOGOUT = "LOGOUT";
    public static final String GET_PLAYERS = "GET_PLAYERS";
    public static final String CHALLENGE = "CHALLENGE";
    public static final String ACCEPT_CHALLENGE = "ACCEPT_CHALLENGE";
    public static final String DECLINE_CHALLENGE = "DECLINE_CHALLENGE";
    public static final String CARD_FLIP = "CARD_FLIP";
    public static final String QUIT_GAME = "QUIT_GAME";
    public static final String REMATCH = "REMATCH";
    public static final String GET_LEADERBOARD = "GET_LEADERBOARD";
    public static final String GET_MATCH_HISTORY = "GET_MATCH_HISTORY"; // NEW
    
    // Server -> Client
    public static final String LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String LOGIN_FAILED = "LOGIN_FAILED";
    public static final String REGISTER_SUCCESS = "REGISTER_SUCCESS";
    public static final String REGISTER_FAILED = "REGISTER_FAILED";
    public static final String PLAYER_LIST = "PLAYER_LIST";
    public static final String CHALLENGE_RECEIVED = "CHALLENGE_RECEIVED";
    public static final String CHALLENGE_ACCEPTED = "CHALLENGE_ACCEPTED";
    public static final String CHALLENGE_DECLINED = "CHALLENGE_DECLINED";
    public static final String GAME_START = "GAME_START";
    public static final String GAME_UPDATE = "GAME_UPDATE";
    public static final String SCORE_UPDATE = "SCORE_UPDATE";
    public static final String GAME_END = "GAME_END";
    public static final String OPPONENT_QUIT = "OPPONENT_QUIT";
    public static final String REMATCH_REQUEST = "REMATCH_REQUEST";
    public static final String REMATCH_ACCEPTED = "REMATCH_ACCEPTED";
    public static final String REMATCH_DECLINED = "REMATCH_DECLINED";
    public static final String LEADERBOARD = "LEADERBOARD";
    public static final String MATCH_HISTORY = "MATCH_HISTORY"; // NEW
    public static final String ACCOUNT_BANNED = "ACCOUNT_BANNED";
    public static final String ERROR = "ERROR";
    public static final String OPPONENT_LEFT_LOBBY = "OPPONENT_LEFT_LOBBY";
    
    // Game difficulties
    public static final String EASY = "EASY";
    public static final String MEDIUM = "MEDIUM";
    public static final String HARD = "HARD";
    
    // Game difficulty configurations
    public static int getCardCount(String difficulty) {
        switch(difficulty) {
            case EASY: return 12; // 6 cặp
            case MEDIUM: return 20; // 10 cặp
            case HARD: return 30; // 15 cặp
            default: return 20;
        }
    }
    
    public static int getTimeLimit(String difficulty) {
        switch(difficulty) {
            case EASY: return 180; // 3 phút
            case MEDIUM: return 240; // 4 phút
            case HARD: return 300; // 5 phút
            default: return 240;
        }
    }
}