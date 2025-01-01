package example.game;

import example.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameFlowerTest {
    @Test
    public void testFlowerDraw() {
        Player player = new Player("Test", true);

        // 加入一般牌
        player.addTile(new Tile(Tile.TileType.WAN, 1));

        // 加入花牌
        Tile flower = new Tile(Tile.TileType.FLOWER, 1);
        player.addTile(flower);

        // 檢查花牌是否被加入到 flowers 列表中
        assertTrue(player.getFlowers().contains(flower));
    }

    @Test
    public void testFlowerSeparation() {
        Player player = new Player("Test", true);

        // 加入一些牌，包括花牌
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.FLOWER, 1));  // 春
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.FLOWER, 2));  // 夏

        // 檢查手牌和花牌數量
        assertEquals(2, player.getHandSize());  // 只有兩張萬子
        assertEquals(2, player.getFlowers().size());  // 兩張花牌
    }

    @Test
    public void testInitialFlowerHandling() {
        // 建立遊戲並開始，確保花牌處理
        Game game = new Game();
        game.startGame();

        // 檢查每個玩家的手牌中是否還有花牌
        for (Player player : game.getPlayers()) {
            for (Tile tile : player.getHand()) {
                assertNotEquals(Tile.TileType.FLOWER, tile.getType());
            }
        }
    }

    @Test
    public void testFlowerCollection() {
        Player player = new Player("Test", true);

        // 添加所有種類的花牌
        for (int i = 1; i <= 8; i++) {
            player.addTile(new Tile(Tile.TileType.FLOWER, i));
        }

        // 檢查所有花牌都在 flowers 列表中
        assertEquals(8, player.getFlowers().size());
        assertEquals(0, player.getHandSize());  // 手牌應該是空的
    }
}