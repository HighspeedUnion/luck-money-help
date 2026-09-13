import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

public class GameApp extends Application {

    private Player p1, p2;
    private Deque<Card> deck;
    private Player currentPlayer, opponent;
    private int turn = 1;
    private boolean gameOver = false;

    // UI 组件
    private Label p1Status, p2Status, turnLabel;
    private HBox handBox;
    private TextArea logArea;
    private Button endTurnBtn;

    @Override
    public void start(Stage stage) {
        initGame();

        p1Status = new Label();
        p2Status = new Label();
        p1Status.setFont(Font.font(16));
        p2Status.setFont(Font.font(16));
        HBox playerBar = new HBox(60, p1Status, p2Status);
        playerBar.setAlignment(Pos.CENTER);
        playerBar.setPadding(new Insets(10));

        turnLabel = new Label();
        turnLabel.setFont(Font.font(18));

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setPrefHeight(220);

        handBox = new HBox(12);
        handBox.setAlignment(Pos.CENTER);
        handBox.setPadding(new Insets(10));
        handBox.setMinHeight(90);

        endTurnBtn = new Button("结束回合");
        endTurnBtn.setOnAction(_ -> endTurn());

        Button restartBtn = new Button("重新开始");
        restartBtn.setOnAction(_ -> restart());

        HBox controls = new HBox(20, endTurnBtn, restartBtn);
        controls.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, playerBar, turnLabel, logArea, handBox, controls);
        root.setPadding(new Insets(15));
        VBox.setVgrow(logArea, Priority.ALWAYS);

        Scene scene = new Scene(root, 720, 620);
        stage.setTitle("火柴杀 最简版 - JavaFX (Java 25)");
        stage.setScene(scene);
        stage.show();

        log("=== 火柴杀 最简版 ===");
        updateUI();
        startTurn();
    }

    private void initGame() {
        p1 = new Player("玩家1", 5);
        p2 = new Player("玩家2", 5);
        deck = createDeck();

        for (int i = 0; i < 3; i++) {
            p1.drawCard(deck.pop());
            p2.drawCard(deck.pop());
        }

        currentPlayer = p1;
        opponent = p2;
        turn = 1;
        gameOver = false;
        if (endTurnBtn != null) endTurnBtn.setDisable(false);
    }

    private void restart() {
        logArea.clear();
        initGame();
        log("=== 新的一局开始 ===");
        updateUI();
        startTurn();
    }

    /** 回合开始：摸牌 */
    private void startTurn() {
        if (gameOver) return;
        log("\n--- 第" + turn + "回合 ---");
        log(currentPlayer.getName() + " 的回合 (HP: " + currentPlayer.getHp() + ")");

        if (!deck.isEmpty()) {
            currentPlayer.drawCard(deck.pop());
            log(currentPlayer.getName() + " 摸了一张牌。");
        } else {
            log("牌堆已空，不再摸牌。");
        }
        updateUI();
    }

    /** 使用手牌 */
    private void useCard(int idx) {
        if (gameOver) return;
        List<Card> hand = currentPlayer.getHand();
        if (idx < 0 || idx >= hand.size()) return;

        Card card = hand.get(idx);

        switch (card.cardType()) {
            case "attack" -> {
                int dmg = card.value();
                opponent.takeDamage(dmg);
                log(currentPlayer.getName() + " 对 " + opponent.getName()
                        + " 造成 " + dmg + " 点伤害！");
                hand.remove(idx);
            }
            case "heal" -> {
                int healAmount = card.value();
                currentPlayer.heal(healAmount);
                log(currentPlayer.getName() + " 恢复了 " + healAmount + " 点生命。");
                hand.remove(idx);
            }
            default -> {
                log("这张牌暂时无法使用。");
                return;
            }
        }

        updateUI();

        if (!opponent.isAlive()) {
            log("\n" + opponent.getName() + " 死亡！" + currentPlayer.getName() + " 获胜！");
            gameOver = true;
            endTurnBtn.setDisable(true);
            handBox.getChildren().clear();
            updateUI();
        }
    }

    private void endTurn() {
        if (gameOver) return;
        Player tmp = currentPlayer;
        currentPlayer = opponent;
        opponent = tmp;
        turn++;
        startTurn();
    }

    /** 刷新 UI */
    private void updateUI() {
        p1Status.setText(p1.getName() + "  HP: " + p1.getHp() + "/" + p1.getMaxHp()
                + (p1 == currentPlayer && !gameOver ? "  ◀ 行动中" : ""));
        p2Status.setText(p2.getName() + "  HP: " + p2.getHp() + "/" + p2.getMaxHp()
                + (p2 == currentPlayer && !gameOver ? "  ◀ 行动中" : ""));

        turnLabel.setText(gameOver
                ? "游戏结束"
                : "第 " + turn + " 回合 - " + currentPlayer.getName() + " 的回合");

        handBox.getChildren().clear();
        List<Card> hand = currentPlayer.getHand();

        for (int i = 0; i < hand.size(); i++) {
            final int idx = i;
            Card card = hand.get(i);

            Button btn = new Button(card.name() + "\n" + card.description());
            btn.setPrefSize(120, 70);
            btn.setWrapText(true);
            btn.setStyle(switch (card.cardType()) {
                case "attack" -> "-fx-background-color: #ffb3b3; -fx-font-size: 12;";
                case "heal"   -> "-fx-background-color: #b3ffb3; -fx-font-size: 12;";
                default       -> "-fx-background-color: #e0e0e0; -fx-font-size: 12;";
            });
            btn.setOnAction(_ -> useCard(idx));
            btn.setDisable(gameOver);
            handBox.getChildren().add(btn);
        }
    }

    private void log(String msg) {
        logArea.appendText(msg + "\n");
        logArea.setScrollTop(Double.MAX_VALUE);
    }

    private Deque<Card> createDeck() {
        List<Card> list = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            list.add(new Card("普通攻击", "attack", 1, "造成1点伤害"));
        for (int i = 0; i < 5; i++)
            list.add(new Card("疗伤", "heal", 1, "恢复1点生命"));
        Collections.shuffle(list);
        return new ArrayDeque<>(list);
    }

    public static void main(String[] args) {
        launch(args);
    }
}