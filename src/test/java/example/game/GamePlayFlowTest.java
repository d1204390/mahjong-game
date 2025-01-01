package example.game;

import example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class GamePlayFlowTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.startGame();
    }


    @Test
    public void testPlayerRotation() {
        Player firstPlayer = game.getCurrentPlayer();

        // 確保一輪後回到第一位玩家
        game.nextPlayer();
        game.nextPlayer();
        game.nextPlayer();
        game.nextPlayer();

        assertEquals(firstPlayer, game.getCurrentPlayer());
    }

    @Test
    public void testGameProgressionWithSkips() {
        int initialTiles = game.getRemainingTiles();

        // 模擬幾回合遊戲進程
        for (int i = 0; i < 4 && game.getCurrentState() != GameState.FINISHED; i++) {
            Player current = game.getCurrentPlayer();

            if (game.getCurrentState() == GameState.DISCARDING
                    && current.isHuman()
                    && !current.getHand().isEmpty()) {
                game.humanDiscard(0);
            }

            if (game.getCurrentState() == GameState.RESPONDING) {
                game.handleResponse("SKIP");
            }
        }

        // 確認牌山數量有變化
        assertTrue(game.getRemainingTiles() < initialTiles,
                "Remaining tiles should decrease as game progresses");
    }

    @Test
    public void testInvalidMoveHandling() {
        Player player = game.getCurrentPlayer();
        GameState initialState = game.getCurrentState();

        // 測試無效的打牌索引
        game.humanDiscard(-1);
        game.humanDiscard(100);

        // 確認遊戲狀態沒有被破壞
        assertEquals(initialState, game.getCurrentState());
        assertEquals(player, game.getCurrentPlayer());
    }


    @Test
    public void testMultipleRoundProgression() {
        int maxRounds = 3;
        int currentRound = 0;
        Player startPlayer = game.getCurrentPlayer();

        while (currentRound < maxRounds && game.getCurrentState() != GameState.FINISHED) {
            GameState state = game.getCurrentState();
            Player current = game.getCurrentPlayer();

            if (state == GameState.DISCARDING && current.isHuman() && !current.getHand().isEmpty()) {
                game.humanDiscard(0);
            } else if (state == GameState.RESPONDING) {
                game.handleResponse("SKIP");
            }

            if (game.getCurrentPlayer() == startPlayer) {
                currentRound++;
            }
        }

        assertTrue(currentRound > 0, "Game should complete at least one round");
        assertTrue(game.getRemainingTiles() < 144, "Some tiles should be used");
    }
}