// GameInitializationTest.java
//測試遊戲初始化
//測試牌山大小
//測試初始手牌
package example.game;

import example.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameInitializationTest {
    @Test
    public void testInitialWallSize() {
        Game game = new Game();
        // 總共應該有 144 張牌 (包含花牌)
        assertEquals(144, game.getRemainingTiles());
    }

    @Test
    public void testInitialPlayerHands() {
        Game game = new Game();
        game.startGame();
        for (Player player : game.getPlayers()) {
            assertTrue(player.getHandSize() >= 16);  // 考慮到花牌補牌，可能超過16張
        }
    }
}