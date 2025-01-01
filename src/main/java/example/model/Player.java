package example.model;

import java.util.*;

public class Player {
    private final String name;
    private final List<Tile> hand;        // 手牌
    private final List<Tile> flowers;      // 花牌
    private final List<Tile> discarded;    // 打出的牌
    private final boolean isHuman;         // 是否為人類玩家
    private final List<Meld> melds;        // 鳴牌組（吃碰槓）

    public Player(String name, boolean isHuman) {
        this.name = name;
        this.isHuman = isHuman;
        this.hand = new ArrayList<>();
        this.flowers = new ArrayList<>();
        this.discarded = new ArrayList<>();
        this.melds = new ArrayList<>();
    }

    // 基本操作方法
    public void addTile(Tile tile) {
        if (tile.getType() == Tile.TileType.FLOWER) {
            flowers.add(tile);
        } else {
            hand.add(tile);
        }
    }

    public void discardTile(int index) {
        if (index >= 0 && index < hand.size()) {
            Tile tile = hand.remove(index);
            discarded.add(tile);
        }
    }

    public boolean hasFlower() {
        return !hand.isEmpty() && hand.get(hand.size() - 1).getType() == Tile.TileType.FLOWER;
    }

    // 移除最後一張花牌並加入花牌列表
    public Tile removeLastFlower() {
        if (hasFlower()) {
            Tile flower = hand.remove(hand.size() - 1);
            flowers.add(flower);
            return flower;
        }
        return null;
    }

    // 排序手牌
    public void sortHand() {
        hand.sort((t1, t2) -> {
            // 先按類型排序
            if (t1.getType() != t2.getType()) {
                return t1.getType().ordinal() - t2.getType().ordinal();
            }
            // 同類型按數字排序
            return t1.getNumber() - t2.getNumber();
        });
    }

    // 顯示手牌（帶索引）
    public String getHandString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(i).append(":").append(hand.get(i)).append(" ");
        }
        return sb.toString();
    }

    // 吃牌判定
    public List<List<Integer>> getChiOptions(Tile tile) {
        if (tile.getType() == Tile.TileType.WIND ||
                tile.getType() == Tile.TileType.DRAGON ||
                tile.getType() == Tile.TileType.FLOWER) {
            return Collections.emptyList();
        }

        List<List<Integer>> options = new ArrayList<>();
        Map<Integer, List<Integer>> numberIndices = new HashMap<>();

        // 記錄手牌中同類型牌的位置
        for (int i = 0; i < hand.size(); i++) {
            Tile t = hand.get(i);
            if (t.getType() == tile.getType()) {
                numberIndices.computeIfAbsent(t.getNumber(), k -> new ArrayList<>()).add(i);
            }
        }

        // 檢查三種可能的吃牌方式
        // 例如：打出3，檢查手上是否有 [1,2] 或 [2,4] 或 [4,5]
        int n = tile.getNumber();

        // n在最後的情況 如：[n-2, n-1, n]
        if (n >= 3) {
            List<Integer> indices = new ArrayList<>();
            boolean found = true;
            for (int i = n - 2; i <= n - 1; i++) {
                if (!numberIndices.containsKey(i)) {
                    found = false;
                    break;
                }
                indices.add(numberIndices.get(i).get(0));
            }
            if (found) options.add(indices);
        }

        // n在中間的情況 如：[n-1, n, n+1]
        if (n >= 2 && n <= 8) {
            List<Integer> indices = new ArrayList<>();
            boolean found = true;
            if (!numberIndices.containsKey(n-1) || !numberIndices.containsKey(n+1)) {
                found = false;
            }
            if (found) {
                indices.add(numberIndices.get(n-1).get(0));
                indices.add(numberIndices.get(n+1).get(0));
                options.add(indices);
            }
        }

        // n在最前的情況 如：[n, n+1, n+2]
        if (n <= 7) {
            List<Integer> indices = new ArrayList<>();
            boolean found = true;
            for (int i = n + 1; i <= n + 2; i++) {
                if (!numberIndices.containsKey(i)) {
                    found = false;
                    break;
                }
                indices.add(numberIndices.get(i).get(0));
            }
            if (found) options.add(indices);
        }

        return options;
    }

    // 碰牌判定
    public List<Integer> getPongIndices(Tile tile) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Tile t = hand.get(i);
            if (t.getType() == tile.getType() && t.getNumber() == tile.getNumber()) {
                indices.add(i);
            }
        }
        return indices.size() >= 2 ? indices : Collections.emptyList();
    }

    // 槓牌判定
    public List<Integer> getKongIndices(Tile tile) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Tile t = hand.get(i);
            if (t.getType() == tile.getType() && t.getNumber() == tile.getNumber()) {
                indices.add(i);
            }
        }
        return indices.size() >= 3 ? indices : Collections.emptyList();
    }

    // 暗槓判定
    public List<List<Integer>> getConcealedKongOptions() {
        Map<String, List<Integer>> groups = new HashMap<>();

        // 收集所有相同的牌
        for (int i = 0; i < hand.size(); i++) {
            Tile tile = hand.get(i);
            String key = tile.getType() + "-" + tile.getNumber();
            groups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        // 找出所有四張相同的牌
        List<List<Integer>> options = new ArrayList<>();
        for (List<Integer> indices : groups.values()) {
            if (indices.size() == 4) {
                options.add(indices);
            }
        }

        return options;
    }

    // 執行吃牌
    public void chi(Tile tile, List<Integer> indices) {
        List<Tile> chiTiles = new ArrayList<>();
        // 從大到小移除，避免索引變化
        indices.sort(Collections.reverseOrder());
        for (int index : indices) {
            chiTiles.add(hand.remove(index));
        }
        chiTiles.add(tile);
        melds.add(new Meld(MeldType.CHI, chiTiles));
    }

    // 執行碰牌
    public void pong(Tile tile, List<Integer> indices) {
        List<Tile> pongTiles = new ArrayList<>();
        indices.sort(Collections.reverseOrder());
        for (int index : indices) {
            pongTiles.add(hand.remove(index));
        }
        pongTiles.add(tile);
        melds.add(new Meld(MeldType.PONG, pongTiles));
    }

    // 執行槓牌
    public void kong(Tile tile, List<Integer> indices) {
        List<Tile> kongTiles = new ArrayList<>();
        indices.sort(Collections.reverseOrder());
        for (int index : indices) {
            kongTiles.add(hand.remove(index));
        }
        kongTiles.add(tile);
        melds.add(new Meld(MeldType.KONG, kongTiles));
    }

    // 執行暗槓
    public void concealedKong(List<Integer> indices) {
        List<Tile> kongTiles = new ArrayList<>();
        indices.sort(Collections.reverseOrder());
        for (int index : indices) {
            kongTiles.add(hand.remove(index));
        }
        melds.add(new Meld(MeldType.CONCEALED_KONG, kongTiles));
    }

    // Getters
    public String getName() {
        return name;
    }

    public List<Tile> getHand() {
        return hand;
    }

    public List<Tile> getFlowers() {
        return flowers;
    }

    public List<Tile> getDiscarded() {
        return discarded;
    }

    public boolean isHuman() {
        return isHuman;
    }

    public int getHandSize() {
        return hand.size();
    }

    public int getFlowerCount() {
        return flowers.size();
    }

    public List<Meld> getMelds() {
        return melds;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", hand=" + getHandString() +
                ", flowers=" + flowers +
                ", discarded=" + discarded +
                ", melds=" + melds +
                '}';
    }
}