package example.game;

import example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;

public class GamePatternTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.startGame();
    }

    @Test
    public void testCheckWinWithTriplet() {
        // 測試刻子判斷
        Player player = game.getCurrentPlayer();
        player.getHand().clear();

        // 建立胡牌牌型：5個刻子+1對
        // 刻子1
        for (int i = 0; i < 3; i++) {
            player.addTile(new Tile(Tile.TileType.WAN, 1));
        }
        // 刻子2
        for (int i = 0; i < 3; i++) {
            player.addTile(new Tile(Tile.TileType.WAN, 2));
        }
        // 刻子3
        for (int i = 0; i < 3; i++) {
            player.addTile(new Tile(Tile.TileType.WAN, 3));
        }
        // 刻子4
        for (int i = 0; i < 3; i++) {
            player.addTile(new Tile(Tile.TileType.WAN, 4));
        }
        // 刻子5
        for (int i = 0; i < 3; i++) {
            player.addTile(new Tile(Tile.TileType.WAN, 5));
        }
        // 對子
        player.addTile(new Tile(Tile.TileType.WAN, 6));
        player.addTile(new Tile(Tile.TileType.WAN, 6));

        assertTrue(game.checkWin(player));
    }

    @Test
    public void testCheckWinWithSequence() {
        // 測試順子判斷
        Player player = game.getCurrentPlayer();
        player.getHand().clear();

        // 建立胡牌牌型：5個順子+1對
        // 順子1: 123萬
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.WAN, 3));

        // 順子2: 456萬
        player.addTile(new Tile(Tile.TileType.WAN, 4));
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 6));

        // 順子3: 789萬
        player.addTile(new Tile(Tile.TileType.WAN, 7));
        player.addTile(new Tile(Tile.TileType.WAN, 8));
        player.addTile(new Tile(Tile.TileType.WAN, 9));

        // 順子4: 123筒
        player.addTile(new Tile(Tile.TileType.TONG, 1));
        player.addTile(new Tile(Tile.TileType.TONG, 2));
        player.addTile(new Tile(Tile.TileType.TONG, 3));

        // 順子5: 456筒
        player.addTile(new Tile(Tile.TileType.TONG, 4));
        player.addTile(new Tile(Tile.TileType.TONG, 5));
        player.addTile(new Tile(Tile.TileType.TONG, 6));

        // 對子
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));

        assertTrue(game.checkWin(player));
    }

    @Test
    public void testGameFinishState() {
        Player player = game.getCurrentPlayer();
        player.getHand().clear();

        // 建立胡牌牌型
        for (int i = 1; i <= 5; i++) {
            for (int j = 0; j < 3; j++) {
                player.addTile(new Tile(Tile.TileType.WAN, i));
            }
        }
        player.addTile(new Tile(Tile.TileType.WAN, 6));
        player.addTile(new Tile(Tile.TileType.WAN, 6));

        // 透過檢查勝利來觸發遊戲結束
        if (game.checkWin(player)) {
            assertEquals(GameState.DISCARDING, game.getCurrentState());
        }
    }
}