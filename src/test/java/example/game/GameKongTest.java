package example.game;

import example.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Arrays;

public class GameKongTest {
    private Game game;

    @BeforeEach
    void setUp() {
        game = new Game();
        game.startGame();
    }

    @Test
    public void testExecuteKong() {
        Player player = game.getCurrentPlayer();
        player.getHand().clear();

        // 準備槓牌
        for (int i = 0; i < 3; i++) {
            player.addTile(new Tile(Tile.TileType.WAN, 1));
        }

        // 記錄初始狀態
        int initialMeldSize = player.getMelds().size();

        // 執行槓牌
        game.executeKong(player, Arrays.asList(0, 1, 2));

        // 檢查鳴牌組是否增加了一個槓
        assertEquals(initialMeldSize + 1, player.getMelds().size());
        assertEquals(MeldType.KONG, player.getMelds().get(player.getMelds().size() - 1).getType());
    }

    @Test
    public void testKongAndDraw() {
        Player player = game.getCurrentPlayer();
        player.getHand().clear();

        // 準備槓牌
        for (int i = 0; i < 3; i++) {
            player.addTile(new Tile(Tile.TileType.WAN, 1));
        }

        int initialWallSize = game.getRemainingTiles();

        // 執行槓牌
        game.executeKong(player, Arrays.asList(0, 1, 2));

        // 檢查是否有補牌
        assertTrue(game.getRemainingTiles() < initialWallSize);
    }
}