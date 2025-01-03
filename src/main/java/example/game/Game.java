package example.game;

import example.model.*;
import java.util.*;

public class Game {
    private final List<Tile> wall;        // 牌山
    private final Player[] players;        // 玩家
    private int currentPlayerIndex;        // 當前玩家索引
    private GameState currentState;        // 當前遊戲狀態
    private Tile lastDiscardedTile;        // 最後打出的牌
    private int lastDiscardedByIndex;      // 最後打出牌的玩家索引
    private Player winner = null;  // 贏家
    private EndType endType = null;  // 結束類型

    public Game() {
        this.wall = new ArrayList<>();
        this.players = new Player[4];
        this.currentPlayerIndex = 0;  // 莊家從0開始
        this.currentState = GameState.WAITING;

        // 初始化玩家（0號為人類玩家）
        players[0] = new Player("Player", true);
        for (int i = 1; i < 4; i++) {
            players[i] = new Player("Computer " + i, false);
        }

        initializeTiles();
    }

    private void initializeTiles() {
        // 初始化數字牌 (萬、筒、條)
        for (Tile.TileType type : new Tile.TileType[]{Tile.TileType.WAN, Tile.TileType.TONG, Tile.TileType.TIAO}) {
            for (int number = 1; number <= 9; number++) {
                for (int i = 0; i < 4; i++) {
                    wall.add(new Tile(type, number));
                }
            }
        }

        // 初始化風牌
        for (int number = 1; number <= 4; number++) {
            for (int i = 0; i < 4; i++) {
                wall.add(new Tile(Tile.TileType.WIND, number));
            }
        }

        // 初始化三元牌
        for (int number = 1; number <= 3; number++) {
            for (int i = 0; i < 4; i++) {
                wall.add(new Tile(Tile.TileType.DRAGON, number));
            }
        }

        // 初始化花牌
        for (int number = 1; number <= 8; number++) {
            wall.add(new Tile(Tile.TileType.FLOWER, number));
        }

        // 洗牌
        Collections.shuffle(wall);
    }

    public void startGame() {
        dealInitialTiles();
        // 發完牌後檢查所有玩家的花牌
        for (Player player : players) {
            while (player.hasFlower()) {
                drawAndAssignTile(player);
            }
            player.sortHand(); // 初始手牌排序
        }
        currentState = GameState.DRAWING;
        System.out.println("Game started!");
        playTurn();
    }

    public void dealInitialTiles() {
        // 發16張牌給每個玩家
        for (int i = 0; i < 16; i++) {
            for (Player player : players) {
                drawAndAssignTile(player);
            }
        }
    }

    private void drawAndAssignTile(Player player) {
        if (!wall.isEmpty()) {
            Tile tile = wall.remove(wall.size() - 1);
            player.addTile(tile);

            // 如果是花牌，需要補牌
            if (tile.getType() == Tile.TileType.FLOWER && !wall.isEmpty()) {
                drawAndAssignTile(player);
            }
            player.sortHand(); // 每次摸牌後排序
        }
    }

    // 執行當前回合
    private void playTurn() {
        Player currentPlayer = getCurrentPlayer();
        System.out.println("\nCurrent player: " + currentPlayer.getName());

        // 顯示所有玩家丟棄的牌
        System.out.println("\nDiscarded tiles:");
        for (Player p : players) {
            System.out.println(p.getName() + ": " + p.getDiscarded());
        }

        // 顯示鳴牌組
        System.out.println("\nMelds:");
        for (Player p : players) {
            System.out.println(p.getName() + ": " + p.getMelds());
        }

        if (wall.isEmpty()) {
            endType = EndType.DRAW;  // 設置結束類型為流局
            currentState = GameState.FINISHED;
            displayFinalState();  // 調用顯示最終狀態
            return;
        }

        // 檢查是否可以暗槓
        if (currentPlayer.isHuman()) {
            List<List<Integer>> kongOptions = currentPlayer.getConcealedKongOptions();
            if (!kongOptions.isEmpty()) {
                System.out.println("Concealed Kong options available:");
                for (int i = 0; i < kongOptions.size(); i++) {
                    List<Integer> indices = kongOptions.get(i);
                    System.out.println(i + ": " + indices.stream()
                            .map(idx -> currentPlayer.getHand().get(idx))
                            .toList());
                }
                System.out.println("Enter K followed by option number to kong, or any other key to skip");
            }
        }

        // 摸牌
        if (currentState == GameState.DRAWING) {
            drawAndAssignTile(currentPlayer);
            // 檢查花牌
            while (currentPlayer.hasFlower() && !wall.isEmpty()) {
                drawAndAssignTile(currentPlayer);
            }

            // 在這裡加入自摸檢查
            if (checkWin(currentPlayer)) {
                System.out.println(currentPlayer.getName() + " wins by self-draw!");
                declareWin(currentPlayer);
                return;
            }

            currentState = GameState.DISCARDING;

            if (currentPlayer.isHuman()) {
                System.out.println("Your hand: " + currentPlayer.getHandString());
                System.out.println("Your flowers: " + currentPlayer.getFlowers());
                System.out.println("Please select a tile to discard (0-" +
                        (currentPlayer.getHand().size()-1) + ")");
            } else {
                autoDiscard(currentPlayer);
                checkResponses();
            }
        }
    }

    private void checkResponses() {
        if (lastDiscardedTile == null) return;
        currentState = GameState.RESPONDING;

        // 先檢查其他玩家是否可以胡牌
        for (int i = 1; i <= 3; i++) {
            int playerIndex = (lastDiscardedByIndex + i) % 4;
            Player player = players[playerIndex];

            // 加入這張牌看看是否能胡
            player.addTile(lastDiscardedTile);
            if (checkWin(player)) {
                if (player.isHuman()) {
                    // 顯示當前手牌
                    System.out.println("\nYour current hand:");
                    System.out.println(player.getHandString());
                    System.out.println("\nDiscarded tile: " + lastDiscardedTile);
                    System.out.println("\nYou can win! Enter 'W' to win, or any other key to skip");
                    player.getHand().remove(player.getHand().size() - 1);
                    return;
                } else {
                    // AI 總是選擇胡牌
                    declareWin(player);
                    return;
                }
            }
            player.getHand().remove(player.getHand().size() - 1);

            // 對於人類玩家，顯示所有可用選項
            if (player.isHuman()) {
                List<String> options = new ArrayList<>();

                // 檢查槓
                List<Integer> kongIndices = player.getKongIndices(lastDiscardedTile);
                if (!kongIndices.isEmpty()) {
                    options.add("K - Kong (槓)");
                }

                // 檢查碰
                List<Integer> pongIndices = player.getPongIndices(lastDiscardedTile);
                if (!pongIndices.isEmpty()) {
                    options.add("P - Pong (碰)");
                }

                // 檢查吃（只有下家才能吃）
                if (playerIndex == (lastDiscardedByIndex + 1) % 4) {
                    List<List<Integer>> chiOptions = player.getChiOptions(lastDiscardedTile);
                    for (int j = 0; j < chiOptions.size(); j++) {
                        // 獲取當前吃牌組合的牌
                        List<Integer> indices = chiOptions.get(j);
                        List<Tile> chiTiles = indices.stream()
                                .map(idx -> player.getHand().get(idx))
                                .collect(java.util.stream.Collectors.toList());

                        // 根據當前打出的牌，重組完整的吃牌組合
                        List<Tile> fullCombination = new ArrayList<>();
                        for (Tile tile : chiTiles) {
                            fullCombination.add(tile);
                        }
                        // 找到正確的位置插入打出的牌
                        int insertPos = 0;
                        for (int k = 0; k < 2; k++) {
                            if (fullCombination.get(k).getNumber() + 1 == lastDiscardedTile.getNumber()) {
                                insertPos = k + 1;
                                break;
                            } else if (fullCombination.get(k).getNumber() - 2 == lastDiscardedTile.getNumber()) {
                                insertPos = 0;
                                break;
                            } else if (fullCombination.get(k).getNumber() - 1 == lastDiscardedTile.getNumber()) {
                                insertPos = k;
                                break;
                            }
                        }
                        fullCombination.add(insertPos, lastDiscardedTile);

                        // 生成可讀性更好的選項描述
                        String optionDesc = String.format("C%d - Chi: %s %s %s (吃)",
                                j,
                                fullCombination.get(0),
                                fullCombination.get(1),
                                fullCombination.get(2));
                        options.add(optionDesc);
                    }
                }

                if (!options.isEmpty()) {
                    // 先顯示當前手牌
                    System.out.println("\nYour current hand:");
                    System.out.println(player.getHandString());
                    // 顯示打出的牌
                    System.out.println("\nDiscarded tile: " + lastDiscardedTile);
                    // 顯示可用選項
                    System.out.println("\nAvailable actions:");
                    for (String option : options) {
                        System.out.println(option);
                    }
                    System.out.println("\nEnter your choice, or any other key to skip");
                    return;
                }
            } else {
                // 電腦玩家維持原有的優先順序邏輯
                List<Integer> kongIndices = player.getKongIndices(lastDiscardedTile);
                if (!kongIndices.isEmpty() && shouldAIKong()) {
                    executeKong(player, kongIndices);
                    return;
                }

                List<Integer> pongIndices = player.getPongIndices(lastDiscardedTile);
                if (!pongIndices.isEmpty() && shouldAIPong()) {
                    executePong(player, pongIndices);
                    return;
                }
            }
        }

        // 檢查下家是否可以吃（只對電腦玩家）
        Player nextPlayer = players[(lastDiscardedByIndex + 1) % 4];
        if (!nextPlayer.isHuman()) {
            List<List<Integer>> chiOptions = nextPlayer.getChiOptions(lastDiscardedTile);
            if (!chiOptions.isEmpty() && shouldAIChi()) {
                executeChi(nextPlayer, chiOptions.get(0));
                return;
            }
        }

        // 如果沒有人響應，進入下一回合
        currentState = GameState.DRAWING;
        nextTurn();
    }

    // AI決策方法
    private boolean shouldAIKong() {
        return true; // 簡單AI總是槓
    }

    private boolean shouldAIPong() {
        return new Random().nextBoolean(); // 簡單AI隨機碰
    }

    private boolean shouldAIChi() {
        return new Random().nextBoolean(); // 簡單AI隨機吃
    }

    // 執行吃牌
    public void executeChi(Player player, List<Integer> indices) {
        // 先從最後打牌玩家的打出牌堆中移除這張牌
        Player lastDiscarder = players[lastDiscardedByIndex];
        lastDiscarder.getDiscarded().remove(lastDiscarder.getDiscarded().size() - 1);

        player.chi(lastDiscardedTile, indices);
        currentPlayerIndex = getPlayerIndex(player);
        currentState = GameState.DISCARDING;
        lastDiscardedTile = null;
        System.out.println(player.getName() + " chi!");

        if (player.isHuman()) {
            System.out.println("Your hand: " + player.getHandString());
            System.out.println("Please select a tile to discard (0-" +
                    (player.getHand().size()-1) + ")");
        } else {
            autoDiscard(player);
            checkResponses();
        }
    }
    // 執行碰牌
    public void executePong(Player player, List<Integer> indices) {
        // 先從最後打牌玩家的打出牌堆中移除這張牌
        Player lastDiscarder = players[lastDiscardedByIndex];
        lastDiscarder.getDiscarded().remove(lastDiscarder.getDiscarded().size() - 1);

        // 執行原本的碰牌邏輯
        player.pong(lastDiscardedTile, indices);
        currentPlayerIndex = getPlayerIndex(player);
        currentState = GameState.DISCARDING;
        lastDiscardedTile = null;
        System.out.println(player.getName() + " pong!");

        if (player.isHuman()) {
            System.out.println("Your hand: " + player.getHandString());
            System.out.println("Please select a tile to discard (0-" +
                    (player.getHand().size()-1) + ")");
        } else {
            autoDiscard(player);
            checkResponses();
        }
    }

    // 執行槓牌
// 執行槓牌
    public void executeKong(Player player, List<Integer> indices) {
        // 先從最後打牌玩家的打出牌堆中移除這張牌
        Player lastDiscarder = players[lastDiscardedByIndex];
        lastDiscarder.getDiscarded().remove(lastDiscarder.getDiscarded().size() - 1);

        player.kong(lastDiscardedTile, indices);
        currentPlayerIndex = getPlayerIndex(player);

        // 補牌並檢查胡牌
        if (!wall.isEmpty()) {
            drawAndAssignTile(player); // 槓牌要補牌

            // 檢查補牌後是否胡牌（槓上開花）
            if (checkWin(player)) {
                System.out.println(player.getName() + " wins after Kong!");
                declareWin(player);
                return;
            }

            // 處理補牌可能抽到的花牌
            while (player.hasFlower() && !wall.isEmpty()) {
                drawAndAssignTile(player);
                // 檢查補花後是否胡牌
                if (checkWin(player)) {
                    System.out.println(player.getName() + " wins after drawing flower!");
                    declareWin(player);
                    return;
                }
            }
        }

        currentState = GameState.DISCARDING;
        lastDiscardedTile = null;
        System.out.println(player.getName() + " kong!");

        if (player.isHuman()) {
            System.out.println("Your hand: " + player.getHandString());
            System.out.println("Please select a tile to discard (0-" +
                    (player.getHand().size()-1) + ")");
        } else {
            autoDiscard(player);
            checkResponses();
        }
    }

    // AI 自動打牌（簡單版本：隨機）
    private void autoDiscard(Player player) {
        if (!player.getHand().isEmpty()) {
            int randomIndex = new Random().nextInt(player.getHand().size());
            Tile discarded = player.getHand().get(randomIndex);
            player.discardTile(randomIndex);
            lastDiscardedTile = discarded;
            lastDiscardedByIndex = currentPlayerIndex;
            System.out.println(player.getName() + " discarded: " + discarded);
        }
    }

    // 人類玩家打牌
    public void humanDiscard(int index) {
        Player player = getCurrentPlayer();
        if (player.isHuman() && currentState == GameState.DISCARDING) {
            if (index >= 0 && index < player.getHand().size()) {
                Tile discarded = player.getHand().get(index);
                player.discardTile(index);
                lastDiscardedTile = discarded;
                lastDiscardedByIndex = currentPlayerIndex;
                System.out.println("You discarded: " + discarded);
                checkResponses();
            } else {
                System.out.println("Invalid tile index!");
            }
        }
    }

    // 處理人類玩家的吃碰槓選擇
    public void handleResponse(String command) {
        if (currentState != GameState.RESPONDING || lastDiscardedTile == null) {
            return;
        }

        // 對每個玩家依序檢查
        for (int i = 1; i <= 3; i++) {
            int playerIndex = (lastDiscardedByIndex + i) % 4;
            Player player = players[playerIndex];

            // 只處理人類玩家的響應
            if (!player.isHuman()) {
                continue;
            }

            // 先處理胡牌
            if (command.equals("W")) {
                player.addTile(lastDiscardedTile);
                if (checkWin(player)) {
                    System.out.println(player.getName() + " wins!");
                    declareWin(player);
                    return;
                }
                player.getHand().remove(player.getHand().size() - 1);
            }

            // 檢查是否有可用的動作
            List<Integer> kongIndices = player.getKongIndices(lastDiscardedTile);
            List<Integer> pongIndices = player.getPongIndices(lastDiscardedTile);
            List<List<Integer>> chiOptions = new ArrayList<>();
            if (playerIndex == (lastDiscardedByIndex + 1) % 4) {
                chiOptions = player.getChiOptions(lastDiscardedTile);
            }

            // 根據玩家的選擇執行相應動作
            if (command.equals("K") && !kongIndices.isEmpty()) {
                executeKong(player, kongIndices);
                return;
            }

            if (command.equals("P") && !pongIndices.isEmpty()) {
                executePong(player, pongIndices);
                return;
            }

            if (command.startsWith("C") && !chiOptions.isEmpty()) {
                try {
                    int option = Integer.parseInt(command.substring(1));
                    if (option >= 0 && option < chiOptions.size()) {
                        executeChi(player, chiOptions.get(option));
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid chi command format!");
                }
            }
        }

        // 如果沒有任何操作被執行，進入下一回合
        System.out.println("Action skipped.");
        currentState = GameState.DRAWING;
        nextTurn();
    }

    private int getPlayerIndex(Player player) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] == player) return i;
        }
        return -1;
    }

    // 進入下一回合
    private void nextTurn() {
        nextPlayer();
        currentState = GameState.DRAWING;
        playTurn();
    }

    public Player getCurrentPlayer() {
        return players[currentPlayerIndex];
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 4;
    }

    public int getRemainingTiles() {
        return wall.size();
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public Player[] getPlayers() {
        return players;
    }

    public boolean checkWin(Player player) {
        // 1. 先算已經有的組數（來自吃、碰、槓）
        int completeSets = player.getMelds().size();

        // 2. 找手牌中的刻子和順子
        List<Tile> hand = new ArrayList<>(player.getHand());

        // 先找刻子
        boolean foundTriplet;
        do {
            foundTriplet = false;
            // 每次找到刻子後重新開始，避免索引越界
            for (int i = 0; i < hand.size() - 2 && !foundTriplet; i++) {
                for (int j = i + 1; j < hand.size() - 1 && !foundTriplet; j++) {
                    for (int k = j + 1; k < hand.size(); k++) {
                        Tile t1 = hand.get(i);
                        Tile t2 = hand.get(j);
                        Tile t3 = hand.get(k);

                        if (isSameTile(t1, t2) && isSameTile(t2, t3)) {
                            completeSets++;
                            hand.remove(k);
                            hand.remove(j);
                            hand.remove(i);
                            foundTriplet = true;
                            break;
                        }
                    }
                }
            }
        } while (foundTriplet && hand.size() >= 3);

        // 再找順子
        boolean foundSequence;
        do {
            foundSequence = false;
            // 每次找到順子後重新開始
            outerLoop:
            for (int i = 0; i < hand.size(); i++) {
                Tile t1 = hand.get(i);
                if (t1.getType() == Tile.TileType.DRAGON ||
                        t1.getType() == Tile.TileType.WIND ||
                        t1.getType() == Tile.TileType.FLOWER) {
                    continue;
                }

                // 找第二張牌
                for (int j = 0; j < hand.size(); j++) {
                    if (i == j) continue;
                    Tile t2 = hand.get(j);
                    if (t2.getType() == t1.getType() &&
                            t2.getNumber() == t1.getNumber() + 1) {
                        // 找第三張牌
                        for (int k = 0; k < hand.size(); k++) {
                            if (k == i || k == j) continue;
                            Tile t3 = hand.get(k);
                            if (t3.getType() == t1.getType() &&
                                    t3.getNumber() == t1.getNumber() + 2) {
                                completeSets++;
                                // 從大到小移除
                                int max = Math.max(Math.max(i, j), k);
                                int min = Math.min(Math.min(i, j), k);
                                int mid = i + j + k - max - min;
                                hand.remove(max);
                                hand.remove(mid);
                                hand.remove(min);
                                foundSequence = true;
                                break outerLoop;
                            }
                        }
                    }
                }
            }
        } while (foundSequence && hand.size() >= 3);

        // 3. 找對子
        boolean hasAPair = false;
        if (hand.size() == 2) {
            hasAPair = isSameTile(hand.get(0), hand.get(1));
        }

        // 4. 判斷：有5組 + 1對就是胡牌
        return completeSets == 5 && hasAPair;
    }

    private boolean isSameTile(Tile t1, Tile t2) {
        return t1.getType() == t2.getType() && t1.getNumber() == t2.getNumber();
    }

    // 檢查是否能組成指定數量的順子或刻子
    private boolean canFormSetsAndSequences(List<Tile> tiles, int requiredSets) {
        if (requiredSets == 0) return tiles.isEmpty();
        if (tiles.isEmpty()) return false;

        List<Tile> remainingTiles = new ArrayList<>(tiles);

        // 嘗試組成刻子
        if (tiles.size() >= 3) {
            if (canFormTriplet(tiles.get(0), remainingTiles)) {
                remainingTiles.remove(0);
                remainingTiles.remove(0);
                remainingTiles.remove(0);
                if (canFormSetsAndSequences(remainingTiles, requiredSets - 1)) {
                    return true;
                }
                // 恢復移除的牌
                remainingTiles.addAll(tiles.subList(0, 3));
            }
        }

        // 嘗試組成順子
        if (canFormSequence(tiles.get(0), remainingTiles)) {
            // 找到並移除順子的三張牌
            Tile first = tiles.get(0);
            remainingTiles.removeIf(t ->
                    t.getType() == first.getType() &&
                            (t.getNumber() == first.getNumber() ||
                                    t.getNumber() == first.getNumber() + 1 ||
                                    t.getNumber() == first.getNumber() + 2));

            if (canFormSetsAndSequences(remainingTiles, requiredSets - 1)) {
                return true;
            }
        }

        return false;
    }

    // 檢查是否能組成刻子
    private boolean canFormTriplet(Tile firstTile, List<Tile> tiles) {
        int count = 1;
        for (Tile t : tiles) {
            if (isSameTile(firstTile, t)) count++;
            if (count >= 3) return true;
        }
        return false;
    }

    // 檢查是否能組成順子
    private boolean canFormSequence(Tile firstTile, List<Tile> tiles) {
        if (firstTile.getType() == Tile.TileType.DRAGON ||
                firstTile.getType() == Tile.TileType.WIND) {
            return false;
        }

        boolean hasNext = false;
        boolean hasNextNext = false;
        int targetNumber = firstTile.getNumber();

        for (Tile t : tiles) {
            if (t.getType() == firstTile.getType()) {
                if (t.getNumber() == targetNumber + 1) hasNext = true;
                if (t.getNumber() == targetNumber + 2) hasNextNext = true;
            }
        }

        return hasNext && hasNextNext;
    }

    // 宣告胡牌
    public void declareWin(Player player) {
        winner = player;
        endType = EndType.WIN;
        currentState = GameState.FINISHED;
        displayFinalState();
    }

    // 檢查遊戲是否結束
    private void checkGameEnd() {
        if (currentState == GameState.DISCARDING) {
            if (checkWin(getCurrentPlayer())) {
                declareWin(getCurrentPlayer());
                return;
            }
        }
        if (wall.isEmpty()) {
            endType = EndType.DRAW;
            currentState = GameState.FINISHED;
            displayFinalState();
        }
    }

    // 顯示最終狀態
    private void displayFinalState() {
        System.out.println("\n=== Game Over ===");
        if (endType == EndType.WIN) {
            System.out.println("Winner: " + winner.getName());
        } else {
            System.out.println("Game ended in a draw (流局)");
        }

        checkTotalTiles();

        System.out.println("\n=== Final State for Each Player ===");
        for (Player player : players) {
            System.out.println("\n" + player.getName() + ":");
            System.out.println("Hand: " + player.getHandString());
            System.out.println("Flowers: " + player.getFlowers());
            System.out.println("Melds: " + player.getMelds());
            System.out.println("Discarded: " + player.getDiscarded());
        }

        System.out.println("\nRemaining tiles in wall: " + wall.size());
        System.out.println("===============================");
    }

    // 檢查牌的總數是否正確
    private void checkTotalTiles() {
        Map<String, Integer> tileCount = new HashMap<>();
        int totalCount = 0;

        // 計算牌山中的牌
        for (Tile tile : wall) {
            String key = tile.getType() + "-" + tile.getNumber();
            tileCount.put(key, tileCount.getOrDefault(key, 0) + 1);
            totalCount++;
        }

        // 計算所有玩家的牌（手牌、鳴牌組、打出的牌、花牌）
        for (Player player : players) {
            // 計算手牌
            for (Tile tile : player.getHand()) {
                String key = tile.getType() + "-" + tile.getNumber();
                tileCount.put(key, tileCount.getOrDefault(key, 0) + 1);
                totalCount++;
            }

            // 計算花牌
            for (Tile tile : player.getFlowers()) {
                String key = tile.getType() + "-" + tile.getNumber();
                tileCount.put(key, tileCount.getOrDefault(key, 0) + 1);
                totalCount++;
            }

            // 計算打出的牌
            for (Tile tile : player.getDiscarded()) {
                String key = tile.getType() + "-" + tile.getNumber();
                tileCount.put(key, tileCount.getOrDefault(key, 0) + 1);
                totalCount++;
            }

            // 計算鳴牌組
            for (Meld meld : player.getMelds()) {
                for (Tile tile : meld.getTiles()) {
                    String key = tile.getType() + "-" + tile.getNumber();
                    tileCount.put(key, tileCount.getOrDefault(key, 0) + 1);
                    totalCount++;
                }
            }
        }

        // 檢查並輸出問題
        System.out.println("\n=== Tile Count Check ===");
        System.out.println("Total tiles: " + totalCount + " (should be 144)");

        for (Map.Entry<String, Integer> entry : tileCount.entrySet()) {
            String[] parts = entry.getKey().split("-");
            Tile.TileType type = Tile.TileType.valueOf(parts[0]);
            int number = Integer.parseInt(parts[1]);
            int count = entry.getValue();

            // 檢查基本牌是否超過4張
            if (type != Tile.TileType.FLOWER && count > 4) {
                System.out.println("Error: " + getTileName(type, number) +
                        " appears " + count + " times (should be <= 4)");
            }
            // 檢查花牌是否超過1張
            else if (type == Tile.TileType.FLOWER && count > 1) {
                System.out.println("Error: " + getTileName(type, number) +
                        " appears " + count + " times (should be 1)");
            }
        }
        System.out.println("======================");
    }

    // 輔助方法：獲取牌的中文名稱
    private String getTileName(Tile.TileType type, int number) {
        if (type == Tile.TileType.WIND) {
            return switch (number) {
                case 1 -> "東";
                case 2 -> "南";
                case 3 -> "西";
                case 4 -> "北";
                default -> "未知風牌";
            };
        } else if (type == Tile.TileType.DRAGON) {
            return switch (number) {
                case 1 -> "中";
                case 2 -> "發";
                case 3 -> "白";
                default -> "未知字牌";
            };
        } else if (type == Tile.TileType.FLOWER) {
            return switch (number) {
                case 1 -> "春";
                case 2 -> "夏";
                case 3 -> "秋";
                case 4 -> "冬";
                case 5 -> "梅";
                case 6 -> "蘭";
                case 7 -> "菊";
                case 8 -> "竹";
                default -> "未知花牌";
            };
        }
        return number + type.getChinese();
    }

    // 測試用main方法
    public static void main(String[] args) {
        Game game = new Game();
        game.startGame();

        Scanner scanner = new Scanner(System.in);
        while (game.getCurrentState() != GameState.FINISHED) {
            if (game.getCurrentState() == GameState.DISCARDING &&
                    game.getCurrentPlayer().isHuman()) {
                // 打牌階段
                System.out.print("Enter a number to discard: ");
                try {
                    int index = scanner.nextInt();
                    game.humanDiscard(index);
                } catch (InputMismatchException e) {
                    scanner.nextLine();
                    System.out.println("Please enter a valid number!");
                }
            }
            else if (game.getCurrentState() == GameState.RESPONDING) {
                // 吃碰槓階段
                System.out.print("Enter your action (P/K/C0/C1 or other to skip): ");
                String command = scanner.next().toUpperCase();
                game.handleResponse(command);
            }

            // 給一點時間顯示輸出
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        scanner.close();
    }
}