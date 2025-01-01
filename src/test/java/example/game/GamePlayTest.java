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
    public void testDiscardTile() {
        game.startGame();
        Player player = game.getCurrentPlayer();

        // 確保是人類玩家且在打牌階段
        assertTrue(player.isHuman(), "當前玩家應該是人類");
        assertEquals(GameState.DISCARDING, game.getCurrentState(), "遊戲狀態應該是DISCARDING");

        // 找到第一個非花牌的牌的索引
        int discardIndex = -1;
        for (int i = 0; i < player.getHand().size(); i++) {
            if (player.getHand().get(i).getType() != Tile.TileType.FLOWER) {
                discardIndex = i;
                break;
            }
        }

        // 確保找到了非花牌
        assertNotEquals(-1, discardIndex, "應該找到非花牌");

        // 記錄初始狀態
        int initialHandSize = player.getHandSize();
        int initialDiscardedSize = player.getDiscarded().size();
        Tile tileToDiscard = player.getHand().get(discardIndex);

        // 執行棄牌
        game.humanDiscard(discardIndex);

        // 驗證棄牌後的狀態
        assertEquals(initialHandSize - 1, player.getHandSize(), "手牌數量應該減少1");
        assertEquals(initialDiscardedSize + 1, player.getDiscarded().size(), "已棄牌數量應該增加1");

        // 檢查最後一張棄牌
        Tile lastDiscardedTile = player.getDiscarded().get(player.getDiscarded().size() - 1);
        assertEquals(tileToDiscard, lastDiscardedTile, "棄牌應該與選擇的牌匹配");

        // 確保棄牌不是花牌
        assertNotEquals(Tile.TileType.FLOWER, lastDiscardedTile.getType(), "不應棄出花牌");
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