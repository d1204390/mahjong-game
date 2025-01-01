package example.game;

import example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
    }

    @Test
    @DisplayName("測試遊戲初始化")
    void testGameInitialization() {
        // 驗證遊戲初始狀態
        assertEquals(GameState.WAITING, game.getCurrentState(), "初始狀態應為WAITING");
        assertNotNull(game.getPlayers(), "玩家陣列不應為空");
        assertEquals(4, game.getPlayers().length, "應該有4位玩家");
        assertTrue(game.getPlayers()[0].isHuman(), "第一位玩家應該是人類玩家");
        assertFalse(game.getPlayers()[1].isHuman(), "第二位玩家應該是電腦");
    }

    @Test
    @DisplayName("測試發牌")
    void testDealTiles() {
        game.startGame();

        for (Player player : game.getPlayers()) {
            // 檢查手牌數量是否正確（考慮花牌的情況）
            int totalTiles = player.getHand().size() + player.getFlowers().size();
            assertTrue(totalTiles >= 16, "每位玩家應該至少有16張牌(包含可能的花牌)");
        }
    }

    @Test
    @DisplayName("測試槓牌流程")
    void testKongProcess() {
        game.startGame();
        Player player = game.getPlayers()[0];

        // 清空現有手牌
        while (!player.getHand().isEmpty()) {
            player.discardTile(0);
        }

        // 設置槓牌情況
        Tile tile = new Tile(Tile.TileType.DRAGON, 1);  // 中
        for (int i = 0; i < 3; i++) {
            player.addTile(tile);
        }

        // 執行槓
        List<Integer> kongIndices = List.of(0, 1, 2);
        game.executeKong(player, kongIndices);

        // 驗證槓後的狀態
        assertEquals(1, player.getMelds().size(), "應該有一個鳴牌組");
        assertEquals(MeldType.KONG, player.getMelds().get(0).getType(), "應該是槓");
    }

    @Test
    @DisplayName("測試胡牌判定")
    void testWinCondition() {
        game.startGame();
        Player player = game.getPlayers()[0];

        // 清空手牌
        while (!player.getHand().isEmpty()) {
            player.discardTile(0);
        }

        // 加入四組順子
        // 123萬
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.WAN, 3));

        // 456萬
        player.addTile(new Tile(Tile.TileType.WAN, 4));
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 6));

        // 789筒
        player.addTile(new Tile(Tile.TileType.TONG, 7));
        player.addTile(new Tile(Tile.TileType.TONG, 8));
        player.addTile(new Tile(Tile.TileType.TONG, 9));

        // 刻子
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));

        // 將牌對子
        player.addTile(new Tile(Tile.TileType.WIND, 1));
        player.addTile(new Tile(Tile.TileType.WIND, 1));

        assertTrue(game.checkWin(player), "應該要能胡牌");
    }


}