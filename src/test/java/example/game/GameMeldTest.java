package example.game;

import example.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class GameMeldTest {
    @Test
    public void testPong() {
        Player player = new Player("Test", true);
        // 設置碰牌情境並測試
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 1));

        List<Integer> indices = player.getPongIndices(new Tile(Tile.TileType.WAN, 1));
        assertFalse(indices.isEmpty());
    }

    @Test
    public void testChi() {
        Player player = new Player("Test", true);
        // 設置吃牌情境並測試
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 2));

        List<List<Integer>> options = player.getChiOptions(new Tile(Tile.TileType.WAN, 3));
        assertFalse(options.isEmpty());
    }
}