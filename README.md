# 軟體品質測試課程 - 麻將遊戲測試實作報告

## 專案選擇與開發動機

在課程初期，老師建議我們選擇一個邏輯性強的程式進行開發和測試。觀察到其他同學多選擇撲克牌類遊戲，我認為麻將是一個很好的選擇：它有複雜的規則邏輯，需要處理多種牌型組合，還涉及多人互動，是實踐軟體測試的理想專案。

## 開發過程中的挑戰

### 1. 規則界定的困難
麻將規則非常複雜，在實作過程中遇到許多需要明確定義的細節：
- 花牌補牌的時機處理
- 吃碰槓的優先順序
- 和牌判定的複雜度
- 特殊牌型的處理

這些規則的實作都需要仔細的測試來驗證正確性。

### 2. AI設計的取捨
在設計AI時，我面臨了一個兩難：是做一個很強的AI，還是符合一般玩家水準的AI？最終我選擇了後者，因為：
- 開發時間有限
- 一般玩家更容易接受
- 測試更容易進行

### 3. 遊戲環節的連貫性
在開發過程中，發現了許多流程上的問題：
```java
// 最初的版本存在問題
public void handleResponse(String command) {
    if (currentState == GameState.RESPONDING) {
        // 沒有考慮到所有可能的回應情況
        processResponse(command);
        nextTurn();  // 直接進入下一回合
    }
}

// 改進後的版本
public void handleResponse(String command) {
    if (currentState != GameState.RESPONDING || lastDiscardedTile == null) {
        return;
    }
    
    // 按優先順序檢查每個玩家的可能動作
    for (int i = 1; i <= 3; i++) {
        int playerIndex = (lastDiscardedByIndex + i) % 4;
        Player player = players[playerIndex];
        
        // 處理各種可能的響應
        if (command.equals("W") && checkWin(player)) {
            declareWin(player);
            return;
        }
        // ... 其他響應的處理
    }
}
```

## 測試方法與工具應用

### 1. 單元測試（JUnit 5）
我主要使用 JUnit 5 進行單元測試，針對不同功能模組設計了多個測試類別：

```java
// 遊戲初始化測試
@Test
public void testInitialWallSize() {
    Game game = new Game();
    assertEquals(144, game.getRemainingTiles());
}

// 花牌處理測試
@Test
public void testFlowerDraw() {
    Player player = new Player("Test", true);
    player.addTile(new Tile(Tile.TileType.FLOWER, 1));
    assertTrue(player.getFlowers().size() == 1);
}
```

### 2. 代碼覆蓋率分析（JaCoCo）
使用 JaCoCo 工具追蹤代碼覆蓋率：
- 在 pom.xml 中配置 JaCoCo 插件
- 設定覆蓋率目標
- 通過視覺化報告查看測試覆蓋情況

### 3. 靜態代碼分析（PMD）
透過 PMD 進行代碼質量控制：
- 檢查代碼風格
- 發現潛在的代碼問題
- 確保代碼品質標準

### 4. 集成測試
針對完整遊戲流程進行測試：
```java
@Test
public void testCompleteGameFlow() {
    Game game = new Game();
    game.startGame();
    
    // 模擬完整遊戲流程
    while (game.getCurrentState() != GameState.FINISHED) {
        simulateGameTurn(game);
        verifyGameState(game);
    }
}
```

### 5. 邊界條件測試
特別注意測試各種極限情況：
```java
@Test
public void testEdgeCases() {
    // 測試牌山耗盡的情況
    while (!game.wall.isEmpty()) {
        game.drawTile();
    }
    assertEquals(EndType.DRAW, game.endType);
    
    // 測試非法操作
    game.humanDiscard(-1);
    game.humanDiscard(100);
}
```

### 6. 狀態轉換測試
使用狀態圖指導測試用例設計：
```java
@Test
public void testStateTransitions() {
    assertEquals(GameState.WAITING, game.getCurrentState());
    game.startGame();
    assertEquals(GameState.DRAWING, game.getCurrentState());
    // ... 測試其他狀態轉換
}
```

## 測試過程中發現的問題

### 1. 牌數不正確
最初的測試發現牌的總數時常不對，通過編寫專門的測試方法找出問題：
```java
@Test
public void testTotalTilesCount() {
    Map<String, Integer> tileCount = new HashMap<>();
    int totalCount = countAllTiles(tileCount);
    assertEquals(144, totalCount, "Total tiles should be 144");
    
    // 檢查每種牌的數量是否正確
    for (Map.Entry<String, Integer> entry : tileCount.entrySet()) {
        if (!entry.getKey().startsWith("FLOWER")) {
            assertTrue(entry.getValue() <= 4, 
                "Non-flower tiles should not exceed 4: " + entry.getKey());
        }
    }
}
```

### 2. 遊戲無法正常結束
在測試過程中發現某些情況下遊戲無法正常結束，例如：
- 荒牌時沒有正確處理
- 某些和牌情況沒有被檢測到
- 玩家反應處理的邏輯錯誤

通過增加狀態轉換的測試解決了這些問題：
```java
@Test
public void testGameEndConditions() {
    // 測試荒牌結束
    while (game.getRemainingTiles() > 0) {
        game.drawTile();
    }
    assertEquals(GameState.FINISHED, game.getCurrentState());
    assertEquals(EndType.DRAW, game.getEndType());
}
```

## 測試心得

透過這次的實作，我深刻體會到測試的重要性：

1. **測試驅動開發的價值**
   - 測試幫助我更清楚地定義需求
   - 發現了許多原本沒想到的邊界情況
   - 提高了代碼的可靠性

2. **自動化測試的效率**
   - 節省了大量手動測試時間
   - 能快速驗證修改後的功能
   - 提供了可靠的品質保證

3. **測試案例設計的經驗**
   - 學會從玩家角度設計測試
   - 重視邊界條件的測試
   - 理解了測試覆蓋率的意義

## 未來改進方向

1. **擴展測試範圍**
   - 加入更多特殊牌型的測試
   - 增加AI策略的測試案例
   - 完善流程測試的覆蓋率

2. **改進遊戲機制**
   - 優化AI的策略
   - 增加更多麻將玩法變體
   - 改善遊戲流程的流暢度

## 結論

這次的麻將遊戲開發和測試實作，讓我學到了很多實務經驗。測試不只是驗證功能正確性，更是幫助改進設計的重要工具。通過這個項目，我更加理解了軟體品質保證的重要性，也學會了如何更有效地進行軟體測試。
