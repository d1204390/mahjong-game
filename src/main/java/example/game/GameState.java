package example.game;

public enum GameState {
    WAITING,        // 等待開始
    DRAWING,        // 摸牌階段
    DISCARDING,     // 打牌階段
    RESPONDING,     // 等待其他玩家響應（吃碰槓）
    FINISHED        // 遊戲結束
}