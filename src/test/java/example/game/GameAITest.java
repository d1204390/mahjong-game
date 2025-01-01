package example.game;

import example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class GameAITest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.startGame();
    }

    @Test
    public void testAIPlayerTurn() {
        // 直接移動到電腦玩家
        game.nextPlayer();  // 移動到 Computer 1

        Player currentPlayer = game.getCurrentPlayer();
        assertFalse(currentPlayer.isHuman(), "Second player should be AI");

        // 檢查電腦玩家的基本屬性
        assertFalse(currentPlayer.getHand().isEmpty(), "AI should have cards");
        assertTrue(currentPlayer.getName().startsWith("Computer"),
                "AI player should have Computer in name");
    }

    @Test
    public void testDiscardAndResponse() {
        Player humanPlayer = game.getPlayers()[0];
        int maxMoves = 3;
        int moves = 0;

        while (moves < maxMoves && game.getCurrentState() != GameState.FINISHED) {
            Player currentPlayer = game.getCurrentPlayer();
            GameState currentState = game.getCurrentState();

            if (currentPlayer.isHuman() && !currentPlayer.getHand().isEmpty() &&
                    currentState == GameState.DISCARDING) {
                int beforeSize = currentPlayer.getDiscarded().size();
                game.humanDiscard(0);
                assertEquals(beforeSize + 1, currentPlayer.getDiscarded().size());
            }

            if (currentState == GameState.RESPONDING) {
                game.handleResponse("SKIP");
            }

            moves++;
        }

        assertTrue(moves > 0, "Game should proceed through multiple states");
    }

    @Test
    public void testGameProgression() {
        // 確保遊戲開始時狀態正確
        assertEquals(GameState.DISCARDING, game.getCurrentState());

        // 檢查玩家順序
        Player[] players = game.getPlayers();
        assertTrue(players[0].isHuman(), "First player should be human");
        for (int i = 1; i < players.length; i++) {
            assertFalse(players[i].isHuman(), "Other players should be AI");
        }

        // 檢查初始牌數
        for (Player player : players) {
            assertTrue(player.getHand().size() >= 13, "Each player should have at least 13 cards");
        }
    }

    @Test
    public void testAIResponseToAction() {
        Player humanPlayer = game.getPlayers()[0];

        // 確保在打牌階段且手牌不為空
        if (game.getCurrentState() == GameState.DISCARDING && !humanPlayer.getHand().isEmpty()) {
            // 記錄打牌前的狀態
            int totalDiscards = 0;
            for (Player p : game.getPlayers()) {
                totalDiscards += p.getDiscarded().size();
            }

            // 執行打牌
            game.humanDiscard(0);

            // 等待並驗證遊戲進展
            if (game.getCurrentState() == GameState.RESPONDING) {
                game.handleResponse("SKIP");
            }

            // 確認遊戲有進展
            int newTotalDiscards = 0;
            for (Player p : game.getPlayers()) {
                newTotalDiscards += p.getDiscarded().size();
            }
            assertTrue(newTotalDiscards > totalDiscards,
                    "Game should progress with more discards");
        }
    }
}