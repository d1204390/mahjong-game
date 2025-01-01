// GamePlayTest.java
//測試基本遊戲流程
//測試打牌功能
//測試遊戲狀態轉換

package example.game;

import example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GamePlayTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    public void testInitialState() {
        assertEquals(GameState.WAITING, game.getCurrentState());
    }


    @Test
    public void testNextPlayer() {
        game.startGame();
        Player firstPlayer = game.getCurrentPlayer();
        game.nextPlayer();
        Player nextPlayer = game.getCurrentPlayer();
        assertNotEquals(firstPlayer, nextPlayer);
    }

    @Test
    public void testCurrentPlayerIndex() {
        game.startGame();
        Player[] players = game.getPlayers();
        Player currentPlayer = game.getCurrentPlayer();
        assertEquals(players[0], currentPlayer);  // 第一個玩家應該是莊家
    }

    @Test
    public void testDiscardedTileTracking() {
        game.startGame();
        Player player = game.getCurrentPlayer();
        if (player.isHuman() && game.getCurrentState() == GameState.DISCARDING) {
            int discardIndex = 0;
            Tile discardedTile = player.getHand().get(discardIndex);
            game.humanDiscard(discardIndex);
            assertEquals(discardedTile, player.getDiscarded().get(player.getDiscarded().size() - 1));
        }
    }
}