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
            currentState = GameState.FINISHED;
            System.out.println("Game Over - No more tiles!");
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

    // 檢查其他玩家是否可以吃碰槓
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
                    System.out.println("You can win! Enter 'W' to win, or any other key to skip");
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
                        options.add("C" + j + " - Chi Option " + j + " (吃)");
                    }
                }

                if (!options.isEmpty()) {
                    System.out.println("Available actions:");
                    for (String option : options) {
                        System.out.println(option);
                    }
                    System.out.println("Enter your choice, or any other key to skip");
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
    public void executeKong(Player player, List<Integer> indices) {
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

    // 檢查玩家是否胡牌
// 檢查玩家是否胡牌
    public boolean checkWin(Player player) {
        List<Tile> hand = player.getHand();
        List<Meld> melds = player.getMelds();

        // 計算已經成組的順子/刻子數量（來自吃碰槓）
        int completeSets = melds.size();

        // 如果已經有的組合加上手牌不足以構成四組順/刻子加一對，直接返回false
        if (completeSets * 3 + hand.size() != 17) {
            return false;
        }

        // 還需要找到的順子/刻子數量
        int remainingSets = 4 - completeSets;

        // 複製一個新的手牌列表來進行檢查
        List<Tile> checkHand = new ArrayList<>(hand);
        checkHand.sort((t1, t2) -> {
            if (t1.getType() != t2.getType()) {
                return t1.getType().ordinal() - t2.getType().ordinal();
            }
            return t1.getNumber() - t2.getNumber();
        });

        // 尋找所有可能的對子（將）
        for (int i = 0; i < checkHand.size() - 1; i++) {
            if (isSameTile(checkHand.get(i), checkHand.get(i + 1))) {
                // 找到對子，移除這兩張牌
                List<Tile> remainingTiles = new ArrayList<>(checkHand);
                remainingTiles.remove(i);
                remainingTiles.remove(i);

                // 檢查剩餘的牌是否能組成所需的順子或刻子
                if (canFormSetsAndSequences(remainingTiles, remainingSets)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 檢查兩張牌是否相同
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
        }
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