package example.game;

import example.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

public class GameWinTest {

    @Test
    public void testWinWithAllPongs() {
        // 測試全刻子胡牌
        Player player = new Player("Test", true);

        // 加入5個刻子
        // 刻子1: 三個1萬
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 1));

        // 刻子2: 三個2萬
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.WAN, 2));

        // 刻子3: 三個3萬
        player.addTile(new Tile(Tile.TileType.WAN, 3));
        player.addTile(new Tile(Tile.TileType.WAN, 3));
        player.addTile(new Tile(Tile.TileType.WAN, 3));

        // 刻子4: 三個4萬
        player.addTile(new Tile(Tile.TileType.WAN, 4));
        player.addTile(new Tile(Tile.TileType.WAN, 4));
        player.addTile(new Tile(Tile.TileType.WAN, 4));

        // 刻子5: 三個5萬
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 5));

        // 對子: 兩個6萬
        player.addTile(new Tile(Tile.TileType.WAN, 6));
        player.addTile(new Tile(Tile.TileType.WAN, 6));

        Game game = new Game();
        assertTrue(game.checkWin(player));
    }

    @Test
    public void testWinWithAllChis() {
        // 測試全順子胡牌
        Player player = new Player("Test", true);

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

        // 對子: 兩個中
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));

        Game game = new Game();
        assertTrue(game.checkWin(player));
    }

    @Test
    public void testWinWithMixed() {
        // 測試混合型胡牌（有順子有刻子）
        Player player = new Player("Test", true);

        // 刻子1: 三個1萬
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 1));

        // 順子1: 234萬
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.WAN, 3));
        player.addTile(new Tile(Tile.TileType.WAN, 4));

        // 順子2: 567萬
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 6));
        player.addTile(new Tile(Tile.TileType.WAN, 7));

        // 刻子2: 三個8萬
        player.addTile(new Tile(Tile.TileType.WAN, 8));
        player.addTile(new Tile(Tile.TileType.WAN, 8));
        player.addTile(new Tile(Tile.TileType.WAN, 8));

        // 刻子3: 三個9萬
        player.addTile(new Tile(Tile.TileType.WAN, 9));
        player.addTile(new Tile(Tile.TileType.WAN, 9));
        player.addTile(new Tile(Tile.TileType.WAN, 9));

        // 對子: 兩個發
        player.addTile(new Tile(Tile.TileType.DRAGON, 2));
        player.addTile(new Tile(Tile.TileType.DRAGON, 2));

        Game game = new Game();
        assertTrue(game.checkWin(player));
    }

    @Test
    public void testWinWithMelds() {
        // 測試有吃碰槓的胡牌
        Player player = new Player("Test", true);

        // 加入兩組碰
        List<Tile> pong1 = Arrays.asList(
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1)
        );
        player.getMelds().add(new Meld(MeldType.PONG, pong1));

        List<Tile> pong2 = Arrays.asList(
                new Tile(Tile.TileType.WAN, 2),
                new Tile(Tile.TileType.WAN, 2),
                new Tile(Tile.TileType.WAN, 2)
        );
        player.getMelds().add(new Meld(MeldType.PONG, pong2));

        // 手牌中加入3組
        // 刻子
        player.addTile(new Tile(Tile.TileType.WAN, 3));
        player.addTile(new Tile(Tile.TileType.WAN, 3));
        player.addTile(new Tile(Tile.TileType.WAN, 3));

        // 順子
        player.addTile(new Tile(Tile.TileType.WAN, 4));
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 6));

        // 順子
        player.addTile(new Tile(Tile.TileType.WAN, 7));
        player.addTile(new Tile(Tile.TileType.WAN, 8));
        player.addTile(new Tile(Tile.TileType.WAN, 9));

        // 對子
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));

        Game game = new Game();
        assertTrue(game.checkWin(player));
    }

    @Test
    public void testNotWin() {
        // 測試不能胡牌的情況
        Player player = new Player("Test", true);

        // 加入一些不成組合的牌
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.WAN, 4));
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 7));
        player.addTile(new Tile(Tile.TileType.WAN, 8));
        player.addTile(new Tile(Tile.TileType.TONG, 1));
        player.addTile(new Tile(Tile.TileType.TONG, 2));
        player.addTile(new Tile(Tile.TileType.TONG, 3));
        player.addTile(new Tile(Tile.TileType.TIAO, 1));
        player.addTile(new Tile(Tile.TileType.TIAO, 2));
        player.addTile(new Tile(Tile.TileType.TIAO, 3));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 2));
        player.addTile(new Tile(Tile.TileType.DRAGON, 3));
        player.addTile(new Tile(Tile.TileType.WIND, 1));
        player.addTile(new Tile(Tile.TileType.WIND, 2));

        Game game = new Game();
        assertFalse(game.checkWin(player));
    }

    @Test
    public void testWinWithKong() {
        // 測試有槓的胡牌
        Player player = new Player("Test", true);

        // 加入一個明槓
        List<Tile> kong1 = Arrays.asList(
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1)
        );
        player.getMelds().add(new Meld(MeldType.KONG, kong1));

        // 加入一個暗槓
        List<Tile> kong2 = Arrays.asList(
                new Tile(Tile.TileType.WAN, 2),
                new Tile(Tile.TileType.WAN, 2),
                new Tile(Tile.TileType.WAN, 2),
                new Tile(Tile.TileType.WAN, 2)
        );
        player.getMelds().add(new Meld(MeldType.CONCEALED_KONG, kong2));

        // 加入一個碰
        List<Tile> pong = Arrays.asList(
                new Tile(Tile.TileType.WAN, 3),
                new Tile(Tile.TileType.WAN, 3),
                new Tile(Tile.TileType.WAN, 3)
        );
        player.getMelds().add(new Meld(MeldType.PONG, pong));

        // 手牌中加入2組
        // 順子1: 456萬
        player.addTile(new Tile(Tile.TileType.WAN, 4));
        player.addTile(new Tile(Tile.TileType.WAN, 5));
        player.addTile(new Tile(Tile.TileType.WAN, 6));

        // 順子2: 789萬
        player.addTile(new Tile(Tile.TileType.WAN, 7));
        player.addTile(new Tile(Tile.TileType.WAN, 8));
        player.addTile(new Tile(Tile.TileType.WAN, 9));

        // 對子：中中
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));

        Game game = new Game();
        assertTrue(game.checkWin(player));
    }

    @Test
    public void testWinWithEdgeCases() {
        // 測試邊張順子的胡牌
        Player player = new Player("Test", true);

        // 123萬 (低邊)
        player.addTile(new Tile(Tile.TileType.WAN, 1));
        player.addTile(new Tile(Tile.TileType.WAN, 2));
        player.addTile(new Tile(Tile.TileType.WAN, 3));

        // 789萬 (高邊)
        player.addTile(new Tile(Tile.TileType.WAN, 7));
        player.addTile(new Tile(Tile.TileType.WAN, 8));
        player.addTile(new Tile(Tile.TileType.WAN, 9));

        // 兩個刻子
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));
        player.addTile(new Tile(Tile.TileType.DRAGON, 1));

        player.addTile(new Tile(Tile.TileType.DRAGON, 2));
        player.addTile(new Tile(Tile.TileType.DRAGON, 2));
        player.addTile(new Tile(Tile.TileType.DRAGON, 2));

        // 加入一個碰
        List<Tile> pong = Arrays.asList(
                new Tile(Tile.TileType.DRAGON, 3),
                new Tile(Tile.TileType.DRAGON, 3),
                new Tile(Tile.TileType.DRAGON, 3)
        );
        player.getMelds().add(new Meld(MeldType.PONG, pong));

        // 對子：東東
        player.addTile(new Tile(Tile.TileType.WIND, 1));
        player.addTile(new Tile(Tile.TileType.WIND, 1));

        Game game = new Game();
        assertTrue(game.checkWin(player));
    }

    @Test
    public void testWinWithComplexMixed() {
        // 測試複雜混合型胡牌
        Player player = new Player("Test", true);

        // 加入一個明槓
        List<Tile> kong = Arrays.asList(
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1),
                new Tile(Tile.TileType.WAN, 1)
        );
        player.getMelds().add(new Meld(MeldType.KONG, kong));

        // 加入一個吃
        List<Tile> chi = Arrays.asList(
                new Tile(Tile.TileType.WAN, 2),
                new Tile(Tile.TileType.WAN, 3),
                new Tile(Tile.TileType.WAN, 4)
        );
        player.getMelds().add(new Meld(MeldType.CHI, chi));

        // 加入一個碰
        List<Tile> pong = Arrays.asList(
                new Tile(Tile.TileType.WAN, 5),
                new Tile(Tile.TileType.WAN, 5),
                new Tile(Tile.TileType.WAN, 5)
        );
        player.getMelds().add(new Meld(MeldType.PONG, pong));

        // 手牌中的組合
        // 順子: 678萬
        player.addTile(new Tile(Tile.TileType.WAN, 6));
        player.addTile(new Tile(Tile.TileType.WAN, 7));
        player.addTile(new Tile(Tile.TileType.WAN, 8));

        // 刻子: 999萬
        player.addTile(new Tile(Tile.TileType.WAN, 9));
        player.addTile(new Tile(Tile.TileType.WAN, 9));
        player.addTile(new Tile(Tile.TileType.WAN, 9));

        // 對子：發發
        player.addTile(new Tile(Tile.TileType.DRAGON, 2));
        player.addTile(new Tile(Tile.TileType.DRAGON, 2));

        Game game = new Game();
        assertTrue(game.checkWin(player));
    }
}