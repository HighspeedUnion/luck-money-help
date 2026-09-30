import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;

// ============================================================
// 主入口
// ============================================================
public class GameApp extends Application {

    public static final String UI_VERSION   = "26.2";
    public static final String BUILD_NAME   = "hk";
    public static final String BUILD_NUMBER = "26.2";
    public static final String FULL_TITLE =
            "火柴杀 · UI " + UI_VERSION + " · " + BUILD_NAME + " " + BUILD_NUMBER;

    public static void main(String[] args) { launch(args); }

    private Stage stage;
    private String mode = "single";
    private String pickP1, pickP2;
    private int pickFor = 1;
    private BattleUI ui;
    private BattleEngine engine;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        BattleStats.load();
        RankingSystem.load();
        showMainMenu();
        stage.setTitle(FULL_TITLE);
        stage.show();
    }

    private void showMainMenu() {
        VBox root = new VBox(14);
        root.setAlignment(Pos.CENTER);
        root.setStyle(GameData.BG_GRAD);
        root.setPadding(new Insets(30));

        Label title = new Label("🔥 火柴杀");
        title.setFont(Font.font("System", FontWeight.BOLD, 46));
        title.setTextFill(Color.web("#ffd93d"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#ffd93d", 0.6));
        glow.setRadius(24);
        title.setEffect(glow);

        Label ver = new Label(FULL_TITLE);
        ver.setTextFill(Color.web("#888"));
        ver.setFont(Font.font(12));

        Label stats = new Label("角色 " + GameData.CHARS.size()
                + " · 装备 " + GameData.EQUIPS.size()
                + " · 卡牌 " + CardPool.ALL.size());
        stats.setTextFill(Color.web("#555"));
        stats.setFont(Font.font(11));

        Label rankLbl = new Label(RankingSystem.tierIcon() + " " + RankingSystem.getTier()
                + " · " + RankingSystem.getScore() + "分 · "
                + RankingSystem.getWins() + "胜" + RankingSystem.getLosses() + "负");
        rankLbl.setTextFill(Color.web("#ffd93d"));
        rankLbl.setFont(Font.font(13));

        Button btnAI = menuBtn("🤖 单人 vs AI");
        btnAI.setOnAction(e -> { mode = "single"; showCharSelect(1); });

        Button btnLocal = menuBtn("👥 本地双人");
        btnLocal.setOnAction(e -> { mode = "local"; showCharSelect(1); });

        Button btnDex = ghostBtn("📖 图鉴");
        btnDex.setOnAction(e -> CharDex.show(stage));

        Button btnStats = ghostBtn("📊 对战统计");
        btnStats.setOnAction(e -> BattleStats.showHistory(stage));

        Button btnRank = ghostBtn("🏅 天梯积分");
        btnRank.setOnAction(e -> showRankDetail());

        Button btnQuit = ghostBtn("退出");
        btnQuit.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(title, ver, stats, rankLbl, new Region(),
                btnAI, btnLocal, btnDex, btnStats, btnRank, btnQuit);
        stage.setScene(new Scene(root, 900, 800));
    }

    private void showRankDetail() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("天梯");
        a.setHeaderText(RankingSystem.tierIcon() + " " + RankingSystem.getTier());
        a.setContentText(String.format(
                "积分：%d\n胜场：%d\n负场：%d\n当前连胜：%d\n最高连胜：%d",
                RankingSystem.getScore(), RankingSystem.getWins(),
                RankingSystem.getLosses(), RankingSystem.getStreak(),
                RankingSystem.getBestStreak()));
        a.showAndWait();
    }

    private Button menuBtn(String t) {
        Button b = new Button(t);
        b.setPrefSize(320, 52);
        b.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea, #764ba2);" +
                "-fx-text-fill: white;-fx-font-size: 17px;-fx-font-weight: bold;" +
                "-fx-background-radius: 12;-fx-cursor: hand;");
        DropShadow ds = new DropShadow();
        ds.setColor(Color.web("#00000080"));
        ds.setRadius(10); ds.setSpread(0.3);
        ds.setOffsetX(0); ds.setOffsetY(3);
        b.setEffect(ds);
        return b;
    }

    private Button ghostBtn(String t) {
        Button b = new Button(t);
        b.setPrefWidth(320);
        b.setStyle("-fx-background-color: rgba(255,255,255,0.08);-fx-text-fill: #ccc;" +
                "-fx-font-size: 14px;-fx-background-radius: 12;-fx-padding: 10;" +
                "-fx-cursor: hand;-fx-border-color: rgba(255,255,255,0.15);-fx-border-radius: 12;");
        return b;
    }

    private void showCharSelect(int forPlayer) {
        this.pickFor = forPlayer;
        VBox root = new VBox(12);
        root.setStyle(GameData.BG_GRAD);
        root.setPadding(new Insets(20));

        Label title = new Label(pickFor == 1 ? "玩家1 选择角色" : "玩家2 选择角色");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#ffd93d"));

        FlowPane grid = new FlowPane(8, 8);
        grid.setAlignment(Pos.CENTER);
        for (var e : GameData.CHARS.entrySet()) {
            VBox card = new VBox(3);
            card.setPrefWidth(215);
            card.setPadding(new Insets(10));
            card.setStyle(GameData.CARD_BG);
            Label n = new Label(e.getKey());
            n.setFont(Font.font("System", FontWeight.BOLD, 16));
            n.setTextFill(Color.web("#ffd93d"));
            Label s = new Label(String.format("❤%d ⚔%d 🔮%d",
                    e.getValue().hp(), e.getValue().atk(), e.getValue().matk()));
            s.setTextFill(Color.web("#aaa"));
            s.setFont(Font.font(11));
            Label d = new Label(e.getValue().desc());
            d.setTextFill(Color.web("#bbb")); d.setFont(Font.font(10)); d.setWrapText(true);
            VBox sk = new VBox(1);
            for (var sv : e.getValue().skills().values()) {
                Label sl = new Label("· " + sv.name() + " [CD" + sv.cd() + "]");
                sl.setTextFill(Color.web("#7ec8e3")); sl.setFont(Font.font(9));
                sk.getChildren().add(sl);
            }
            card.getChildren().addAll(n, s, d, sk);
            card.setOnMouseEntered(ev -> card.setStyle(GameData.CARD_ACTIVE));
            card.setOnMouseExited(ev -> card.setStyle(GameData.CARD_BG));
            card.setOnMouseClicked(ev -> pickChar(e.getKey()));
            grid.getChildren().add(card);
        }
        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        root.getChildren().addAll(title, sp);
        stage.setScene(new Scene(root, 1200, 820));
    }

    private void pickChar(String name) {
        if (pickFor == 1) {
            pickP1 = name;
            if (mode.equals("single")) {
                List<String> all = new ArrayList<>(GameData.CHARS.keySet());
                pickP2 = all.get(new Random().nextInt(all.size()));
                startGame();
            } else {
                showCharSelect(2);
            }
        } else {
            pickP2 = name;
            startGame();
        }
    }

    private void startGame() {
        int seed = new Random().nextInt();
        String envId = "clear";
        ui = new BattleUI();
        engine = new BattleEngine(pickP1, pickP2, seed, envId, ui);
        ui.setEngine(engine);
        if (mode.equals("single")) engine.setAI(2);
        stage.setScene(new Scene(ui.getRoot(), 1280, 880));
        engine.start();
    }
}

// ============================================================
// 数据层
// ============================================================
class GameData {
    public static final int HAND_LIMIT_BASE = 20;
    public static final Set<String> DODGE_SKILLS  = Set.of("瞬身闪避", "瞬闪");
    public static final Set<String> SHIELD_SKILLS = Set.of("冰墙", "壁垒守护", "坚防");
    public static final Set<String> DEFENSE_SKILLS;
    static {
        Set<String> s = new HashSet<>(DODGE_SKILLS);
        s.addAll(SHIELD_SKILLS);
        DEFENSE_SKILLS = Set.copyOf(s);
    }

    public static final String BG_GRAD =
            "-fx-background-color: linear-gradient(to bottom right, #0f0c29, #302b63, #24243e);";
    public static final String CARD_BG =
            "-fx-background-color: rgba(255,255,255,0.06);-fx-background-radius: 14;" +
            "-fx-border-color: rgba(255,255,255,0.12);-fx-border-radius: 14;-fx-border-width: 1;";
    public static final String CARD_ACTIVE =
            "-fx-background-color: rgba(255,217,61,0.10);-fx-background-radius: 14;" +
            "-fx-border-color: #ffd93d;-fx-border-radius: 14;-fx-border-width: 2;" +
            "-fx-effect: dropshadow(gaussian, rgba(255,217,61,0.5), 12, 0.5, 0, 0);";

    public record Skill(String name, int cd, String desc) {}
    public record Char(String name, int hp, int atk, int matk, String desc, String passive,
                       Map<String, Skill> skills) {}
    public record Equip(String name, String type, String owner, String desc) {}

    public static final Map<String, Char>  CHARS  = new LinkedHashMap<>();
    public static final Map<String, Equip> EQUIPS = new LinkedHashMap<>();

    static {
        ch("拾荒者",6,1,1,"高坦度装备拉扯坦克","拾获：装备栏每类上限3件",
            sk("劫掠",0,"消耗2张手牌偷对手1装备或1手牌"),
            sk("废土壁垒",5,"1回合免疫普通攻击和技能伤害"));
        ch("毒猎",5,2,1,"持续磨血+资源压制","无",
            sk("噬血",3,"偷对手1血"),
            sk("猎击",3,"2点技能伤害"),
            sk("腐毒侵蚀",5,"中毒3回合，手牌上限永久-1"));
        ch("罗伊",5,1,2,"法伤抗压，自带反伤","无",
            sk("速击",3,"1点技能伤害"),
            sk("坚防",3,"受伤时挡1并反弹1"),
            sk("坚韧蜕变",5,"永久+1HP上限+1物攻"));
        ch("吕山",3,2,1,"手牌爆发+单控","无",
            sk("瞬身闪避",3,"规避1次伤害"),
            sk("电击眩晕",3,"敌方跳过出牌阶段"),
            sk("狂击增幅",5,"下2张攻击手牌伤害翻倍"));
        ch("钟离",2,2,2,"高频小技能输出","无",
            sk("固本",2,"回复1点"),
            sk("剑击",2,"2点技能伤害"),
            sk("火焰灼烧",5,"对全体敌人2点"));
        ch("利刃",3,2,1,"后期成长型输出","无",
            sk("冰墙",3,"受伤时挡1"),
            sk("突刺",3,"1点技能伤害"),
            sk("寒冰增幅",5,"永久+1物攻+1法攻"));
        ch("枪手",4,2,1,"冷却压制+回合强控","无",
            sk("迟滞弹",3,"敌方冷却中技能CD+2"),
            sk("禁锢射击",3,"敌方跳过出牌阶段"),
            sk("时空回溯",5,"回溯到上回合结束"));
        ch("帝郡",4,1,2,"持续减益+伤害转移","无",
            sk("瞬闪",3,"规避1次伤害"),
            sk("焚身灼烧",3,"灼烧3回合"),
            sk("罪罚锁狱",5,"本回合受伤转移给对手"));
        ch("冷锋",3,1,2,"防御兜底+高额法伤","无",
            sk("壁垒守护",3,"受伤时挡1"),
            sk("激光",3,"1点法术技能伤害"),
            sk("浮游炮",5,"3点法术技能伤害"));
        ch("秦默",2,2,2,"手牌博弈，有限复活","无",
            sk("傀儡术",3,"借用敌方1张手牌"),
            sk("锐击",3,"2点技能伤害"),
            sk("复生献祭",5,"阵亡献祭手牌复活（最多2次）"));
        ch("玖恒",4,1,1,"装备拓展+强控+增伤","无",
            sk("拓械",3,"献祭2张手牌+1额外装备槽"),
            sk("锁滞",3,"敌方跳过出牌阶段"),
            sk("战威增幅",5,"本回合所有伤害翻倍"));
        ch("多斯",3,1,1,"全队续航","愈愈光环：己方全体每2回合回复1点",
            sk("愈护",3,"回复1点"),
            sk("速愈调度",3,"自身一技能CD-2"),
            sk("复生仪式",8,"阵亡自动复活（每局1次）"));
        ch("虚无",4,1,3,"高法伤+全场削弱","无",
            sk("虚空爆裂",3,"2点法术+回复1"),
            sk("虚无缠绕",4,"对手下回合手牌上限-2"),
            sk("湮灭",5,"3点法术技能伤害"));
        ch("铁骑",6,2,1,"高血肉盾+嘲讽","无",
            sk("冲锋",3,"2点物理伤害"),
            sk("铁壁",4,"获得2点护盾"),
            sk("势不可挡",6,"本回合物攻翻倍"));
        ch("游侠",3,3,1,"高速单体爆发","无",
            sk("连射",2,"1点伤害可再用一次"),
            sk("致命射击",4,"3点物理伤害"),
            sk("猎鹰之眼",5,"本回合所有攻击+1伤害"));
        ch("圣女",4,1,2,"团队辅助+强力治疗","无",
            sk("圣光",2,"回复2点"),
            sk("净化",3,"移除自身负面"),
            sk("神恩",6,"回复3点+2护盾"));
        ch("影武者",4,2,1,"高闪避刺客","残影：闪避成功后下次攻击+1",
            sk("影袭",3,"2点物理+1闪避"),
            sk("分身术",4,"获得2个闪避"),
            sk("致命偷袭",6,"3点物理，无视护盾"));
        ch("圣殿骑士",7,1,2,"辅助坦克","守护：友方受伤时可代其承受1点",
            sk("神圣壁垒",3,"2护盾+回复1"),
            sk("审判",3,"1点法术+自回1"),
            sk("圣光庇护",6,"2护盾+免疫负面"));
        ch("元素使",3,1,3,"四元素法师","元素共鸣：每用1个技能法攻+1（本回合）",
            sk("烈焰冲击",2,"2点法术+灼烧2"),
            sk("寒冰锁链",3,"1点法术+冻结"),
            sk("雷霆万钧",5,"全体3点法术"));
        ch("赏金猎人",3,3,0,"猎杀输出","悬赏：击杀永久+1物攻+1HP上限",
            sk("标记猎杀",3,"标记敌人，对其伤害+2"),
            sk("陷阱",3,"敌人下回合攻击时反2"),
            sk("致命枪决",6,"对HP<3敌人6点物理"));
        ch("龙骑",5,2,2,"均衡战士","龙血：HP<50%时全属性+1",
            sk("龙息",3,"全体2点法术"),
            sk("龙鳞护体",3,"2护盾+免疫1次物理"),
            sk("龙威",6,"本回合攻翻倍"));
        ch("医者",3,1,2,"治疗+净化","医心：每次治疗额外+1（己方全体）",
            sk("群体治疗",3,"自身和队友各回复1"),
            sk("净化之光",3,"移除己方全体负面"),
            sk("生命链接",6,"本回合承受伤害50%转为治疗"));
        ch("狂战士",5,3,0,"低血高伤","嗜血：每损失1HP，伤害+1（上限+3）",
            sk("狂暴突袭",3,"3点物理+自伤1"),
            sk("血怒",3,"消耗3点血量，本回合伤害翻倍"),
            sk("绝命狂斩",6,"(5-HP)物理伤害，自回2"));
        ch("符卡师",4,1,3,"符卡多面手","符力：回合开始抽牌+1",
            sk("火焰符",2,"1点法术+灼烧1"),
            sk("冰封符",3,"1点法术+冻结"),
            sk("万象符阵",6,"全体2点法术+自抽2"));
        ch("巫医",4,1,2,"中毒控制","诅咒：负面状态时长+1",
            sk("剧毒喷射",3,"1点法术+中毒3"),
            sk("虚弱诅咒",3,"虚弱（下回合伤害减半）"),
            sk("万毒噬心",6,"全体2点法术+中毒3"));
        ch("决斗者",4,2,2,"单挑特化","决斗本能：1v1时伤害+1",
            sk("挑战",3,"2点物理+本回合免疫1次"),
            sk("格挡反击",3,"2护盾，若被攻击反2"),
            sk("终结一击",6,"5点物理+自伤2"));
        ch("占星师",3,1,3,"预言法师","星象：回合开始10%概率下回合CD清零",
            sk("流星",3,"3点法术"),
            sk("命运干预",4,"弃1抽2"),
            sk("星辰陨落",7,"全体4点法术+自身虚弱1回合"));
        ch("武僧",5,2,1,"连击近战","连击：连续攻击同一目标伤害递增",
            sk("连环拳",2,"1点伤害+额外普攻（每回合限1次）"),
            sk("金刚护体",3,"2护盾+回复1"),
            sk("天崩地裂",6,"4点物理+目标下回合攻击-1"));

        eq("加特林","weapon",null,"每回合攻击上限+2");
        eq("防弹背心","armor",null,"每回合首次物理伤害-1");
        eq("能量护盾","armor",null,"获得1点护盾，破后下回合恢复");
        eq("机械猎犬","accessory",null,"攻击时1/3概率+1伤害");
        eq("飞行滑板","accessory",null,"受伤时1/3概率完全闪避");
        eq("重甲犀牛","accessory",null,"未攻击则首次伤害完全免疫");
        eq("幽灵马车","accessory",null,"回合结束，下回合技能/攻击各-1");
        eq("脉冲炮","weapon","冷锋","法攻+1，激光CD-1");
        eq("雷羽弓","weapon","吕山","物攻+1、法攻+1");
        eq("火焰之刃","weapon","钟离","物攻+1、HP上限+1");
        eq("熔岩动力戟","weapon","帝郡","灼烧5回合");
        eq("空间之刃","weapon","玖恒","锁滞双目标");
        eq("寒冰之刃","weapon","利刃","法攻+1、普攻+1、HP上限+1");
        eq("剑匣","weapon","秦默","普攻+1、傀儡双目标");
        eq("剧毒之镰","weapon","毒猎","大招双目标");
        eq("双枪","weapon","枪手","普攻+1、每回合2张攻击");
        eq("创造之墙","armor","罗伊","HP上限+2");
        eq("烟雾掩护","armor","拾荒者","大招+1回合、CD-1");
        eq("医疗包","accessory","多斯","一技能CD-2");
        eq("影之刃","weapon","影武者","闪避后下次攻击+1（可叠至+2）");
        eq("圣殿之盾","armor","圣殿骑士","每回合首次护盾+1");
        eq("元素之心","accessory","元素使","技能伤害+1");
        eq("猎人徽记","accessory","赏金猎人","击杀回2血");
        eq("龙鳞甲","armor","龙骑","HP上限+1、物伤-1、免疫1次/回合");
        eq("医者药箱","accessory","医者","治疗+1");
        eq("狂战项链","accessory","狂战士","伤害+1");
        eq("符卡册","weapon","符卡师","每回合抽牌+1");
        eq("巫毒娃娃","accessory","巫医","负面命中后额外-1HP");
        eq("决斗手枪","weapon","决斗者","1v1时伤害+1");
        eq("星象仪","accessory","占星师","CD概率翻倍");
        eq("武僧念珠","accessory","武僧","连击上限+1");
    }

    private static void ch(String n,int hp,int a,int m,String d,String p,Skill... sks){
        Map<String,Skill> map = new LinkedHashMap<>();
        for (Skill s : sks) map.put(s.name(), s);
        CHARS.put(n, new Char(n,hp,a,m,d,p,map));
    }
    private static Skill sk(String n,int cd,String d){ return new Skill(n,cd,d); }
    private static void eq(String n,String t,String o,String d){ EQUIPS.put(n,new Equip(n,t,o,d)); }
}

// ============================================================
// Card / Equipment
// ============================================================
record Card(String name, String type, int value, String desc, String effect) implements Serializable {
    static Card of(String n, String t, int v, String d) { return new Card(n,t,v,d,null); }
    static Card eff(String n, String t, int v, String d, String e) { return new Card(n,t,v,d,e); }
}

record Equipment(String name, String type, String owner, String desc) implements Serializable {
    static Equipment fromName(String name) {
        GameData.Equip e = GameData.EQUIPS.get(name);
        if (e == null) return new Equipment(name, "accessory", null, "");
        return new Equipment(e.name(), e.type(), e.owner(), e.desc());
    }
}

// ============================================================
// 卡牌池
// ============================================================
class CardPool {
    public static final List<Card> ALL = new ArrayList<>();
    static {
        add("普攻·直击","attack",1,"造成1点伤害");
        add("普攻·重击","attack",2,"造成2点伤害");
        add("普攻·连击","attack",1,"1点+额外普攻");
        add("普攻·快刺","attack",1,"1点+闪避1次");
        add("普攻·横扫","attack",1,"对全体1点");
        add("普攻·牵制","attack",1,"1点+CD+1");
        add("普攻·破袭","attack",2,"2点，无视护盾");
        add("普攻·浴血","attack",2,"2点，自伤1");
        add("物理攻击","physical",0,"等同物攻");
        add("法术攻击","magic",0,"等同法攻");
        add("疗伤","heal",1,"恢复1点");
        add("躲闪","dodge",0,"规避单次伤害");
        add("全能盾牌","omnishield",0,"抵挡一切");
        add("能量护盾卡","shieldcard",0,"+1护盾");
        add("狂暴剂","rage",0,"本回合翻倍");
        add("沉思","meditate",0,"抽5张");
        add("狂暴药水","ragepotion",0,"技能伤害+1");
        add("麻醉剂","anesthetic",0,"跳过完整回合");
        add("急冻","freeze",0,"对手上限-1");
        add("拆除","dismantle",0,"拆除敌方装备");
        add("抢夺","steal",0,"抢夺装备");
        add("净化","cleanse",0,"移除负面");
        add("乱击","chaos",0,"随机1-3伤害");
        add("绝境","desperate",0,"已损失血量伤害");
        add("血祭","sacrifice",0,"-1HP抽3张");
        add("乱心","confuse",0,"对手弃1张");
        add("决斗","duel",0,"决斗3轮");
    }
    private static void add(String n,String t,int v,String d){ ALL.add(Card.of(n,t,v,d)); }

    public static List<Card> createDeck(Random rng){
        List<Card> d = new ArrayList<>();
        for (Card c : ALL){
            int w = switch (c.type()) {
                case "attack" -> c.value() >= 2 ? 4 : 8;
                case "heal","dodge" -> 8;
                default -> 4;
            };
            for (int i=0;i<w;i++) d.add(c);
        }
        for (String name : GameData.EQUIPS.keySet())
            for (int i=0;i<2;i++) d.add(Card.of(name,"equip",0,GameData.EQUIPS.get(name).desc()));
        Collections.shuffle(d, rng);
        return d;
    }
}

// ============================================================
// 玩家
// ============================================================
class Player {
    final String name;
    final String charName;
    final GameData.Char charData;

    int baseAtk, baseMatk, baseMaxHp;
    int atk, matk, maxHp, hp;

    final List<Card> hand = new ArrayList<>();
    final List<Equipment> equipment = new ArrayList<>();
    final Map<String, Integer> cooldowns = new LinkedHashMap<>();

    boolean immuneTurn, skipNextTurn, skipFullTurn;
    int poisonTurns, burnTurns, doubleAttackLeft;
    boolean damageDoubleTurn, meditateUsedThisTurn;
    int extraSlots, energyShieldHp;
    boolean vestUsedThisTurn, rhinoShieldAvailable, carriageBuff, attackedThisTurn;
    int reviveCount, handLimitReduction, dodgeTokens, extraAttackTokens, cdPenalty;
    boolean transferActive;
    Player transferTarget;

    Player(String name, String charName) {
        this.name = name;
        this.charName = charName;
        this.charData = GameData.CHARS.get(charName);
        this.baseMaxHp = charData.hp();
        this.baseAtk = charData.atk();
        this.baseMatk = charData.matk();
        this.hp = baseMaxHp;
        recalcStats(); hp = maxHp;
        for (var e : charData.skills().entrySet()) cooldowns.put(e.getKey(), 0);
    }

    int getHandLimit(){ return Math.max(1, GameData.HAND_LIMIT_BASE - handLimitReduction); }
    boolean isAlive(){ return hp > 0; }
    boolean hasEquip(String n){ return equipment.stream().anyMatch(e -> e.name().equals(n)); }

    void recalcStats(){
        int bAtk=0,bMatk=0,bHp=0;
        for (Equipment e : equipment){
            boolean o = charName.equals(e.owner());
            switch (e.name()){
                case "重甲犀牛" -> bHp += 1;
                case "脉冲炮" -> bMatk += 1;
                case "雷羽弓" -> { bAtk += 1; if (o) bMatk += 1; }
                case "火焰之刃" -> { bAtk += 1; if (o) bHp += 1; }
                case "熔岩动力戟" -> { if (!o) bMatk += 1; }
                case "空间之刃" -> { if (!o) bAtk += 1; }
                case "寒冰之刃" -> { bMatk += 1; if (o) bHp += 1; }
                case "剧毒之镰" -> { if (!o) bAtk += 1; }
                case "创造之墙" -> bHp += o ? 2 : 1;
                case "烟雾掩护" -> { if (!o) bHp += 1; }
                case "影之刃" -> { if (!o) bAtk += 1; }
                case "圣殿之盾" -> { if (!o) bHp += 1; }
                case "元素之心" -> { if (!o) bMatk += 1; }
                case "猎人徽记" -> { if (!o) bAtk += 1; }
                case "龙鳞甲" -> bHp += 1;
                case "狂战项链" -> { if (!o) bAtk += 1; }
                case "符卡册" -> { if (!o) bMatk += 1; }
                case "巫毒娃娃" -> { if (!o) bMatk += 1; }
                case "决斗手枪" -> { if (!o) bAtk += 1; }
                case "星象仪" -> { if (!o) bMatk += 1; }
                case "武僧念珠" -> { if (!o) bAtk += 1; }
            }
        }
        atk = baseAtk + bAtk; matk = baseMatk + bMatk; maxHp = baseMaxHp + bHp;
        if (hp > maxHp) hp = maxHp;
    }

    int equipmentLimit(){ return "拾荒者".equals(charName) ? 3 : 1; }
    int countEquipType(String t){ int c=0; for (Equipment e:equipment) if (e.type().equals(t)) c++; return c; }
    boolean canEquip(Equipment eq){
        int limit = equipmentLimit();
        if ("玖恒".equals(charName)) limit += extraSlots;
        return countEquipType(eq.type()) < limit;
    }
    void equip(Equipment eq){ equipment.add(eq); recalcStats(); }
    boolean drawCard(Card c){
        if (hand.size() < getHandLimit()) { hand.add(c); return true; }
        return false;
    }
    void reduceCooldowns(){
        for (String k : cooldowns.keySet())
            if (cooldowns.get(k) > 0) cooldowns.put(k, cooldowns.get(k) - 1);
    }
    void resetTurnFlags(){
        immuneTurn = false; damageDoubleTurn = false;
        meditateUsedThisTurn = false; vestUsedThisTurn = false; attackedThisTurn = false;
    }
    void onTurnEnd(){
        rhinoShieldAvailable = !attackedThisTurn;
        if (hasEquip("幽灵马车")) carriageBuff = true;
        transferActive = false; transferTarget = null; extraAttackTokens = 0;
    }
}

// ============================================================
// 战斗引擎
// ============================================================
class BattleEngine {
    Player p1, p2, current, opponent;
    int turn = 1;
    boolean over = false;
    final Deque<Card> deck = new ArrayDeque<>();
    final Random rng;
    int countAttacks = 0;
    final BattleUI ui;
    private int aiPlayerIndex = -1;

    BattleEngine(String c1, String c2, int seed, String envId, BattleUI ui) {
        this.ui = ui;
        this.rng = new Random(seed);
        this.p1 = new Player("玩家1", c1);
        this.p2 = new Player("玩家2", c2);
        this.current = p1; this.opponent = p2;
        deck.addAll(CardPool.createDeck(rng));
        for (int i=0;i<3;i++){ p1.drawCard(deck.pop()); p2.drawCard(deck.pop()); }
    }

    void setAI(int idx){ aiPlayerIndex = idx; }

    int getAttackLimit(Player p){
        if (p.hasEquip("加特林")) return 4;
        if ("枪手".equals(p.charName) && p.hasEquip("双枪")) return 3;
        return 2;
    }
    int getNormalAttackBonus(Player p){
        int b = 0;
        if (p.hasEquip("剑匣")) b += 1;
        if ("利刃".equals(p.charName) && p.hasEquip("寒冰之刃")) b += 1;
        if ("枪手".equals(p.charName) && p.hasEquip("双枪")) b += 1;
        return b;
    }
    int equipmentAttackBonus(Player p){
        int x = 0;
        for (Equipment e : p.equipment)
            if ("机械猎犬".equals(e.name()) && rng.nextDouble() < 1.0/3.0){
                ui.log("🐕 机械猎犬触发，+1伤害","skill"); x++;
            }
        return x;
    }

    int applyDamage(Player def, int dmg, Player atk, String type, boolean ignoreShield){
        if (def.transferActive && def.transferTarget != null && def.transferTarget.isAlive()){
            ui.log(String.format("⚖ 罪罚锁狱！%d 伤害转移给 %s", dmg, def.transferTarget.name), "skill");
            def.transferTarget.hp -= dmg; return 0;
        }
        if (def.immuneTurn && type.equals("normal")){ ui.log("🛡 "+def.name+" 免疫","skill"); return 0; }
        if ((type.equals("normal")||type.equals("magic")) && def.carriageBuff){
            dmg = Math.max(0, dmg - 1); def.carriageBuff = false;
            ui.log("🐎 幽灵马车 -1","skill");
        }
        if (type.equals("physical") && def.hasEquip("防弹背心") && !def.vestUsedThisTurn){
            dmg = Math.max(0, dmg - 1); def.vestUsedThisTurn = true;
            ui.log("🦺 防弹背心 -1","skill");
        }
        if (def.hasEquip("重甲犀牛") && def.rhinoShieldAvailable){
            dmg = Math.max(0, dmg - 1); def.rhinoShieldAvailable = false;
            ui.log("🦏 重甲犀牛 -1","skill");
        }
        for (Equipment e : def.equipment)
            if ("飞行滑板".equals(e.name()) && rng.nextDouble() < 1.0/3.0){
                ui.log("🛹 飞行滑板完全闪避","skill"); return 0;
            }
        if (def.energyShieldHp > 0){
            int a = Math.min(def.energyShieldHp, dmg);
            def.energyShieldHp -= a; dmg -= a;
            ui.log(String.format("⚡ 能量护盾吸收 %d（剩 %d）", a, def.energyShieldHp),"skill");
            if (dmg == 0) return 0;
        }

        List<String> opts = new ArrayList<>();
        List<Runnable> acts = new ArrayList<>();
        if (def.dodgeTokens > 0){
            opts.add(String.format("快刺闪避（剩 %d 次）", def.dodgeTokens));
            acts.add(() -> def.dodgeTokens--);
        }
        for (var e : def.charData.skills().entrySet()){
            String sid = e.getKey(); GameData.Skill sv = e.getValue();
            if (GameData.DEFENSE_SKILLS.contains(sv.name()) && def.cooldowns.get(sid) == 0){
                if (ignoreShield && GameData.SHIELD_SKILLS.contains(sv.name())) continue;
                if (type.equals("magic") && GameData.SHIELD_SKILLS.contains(sv.name())) continue;
                opts.add(String.format("技能【%s】", sv.name()));
                final String sn = sv.name(); final String sf = sid;
                acts.add(() -> {
                    if (sn.equals("坚防")){ atk.hp -= 1; ui.log("🛡 坚防反弹1","skill"); }
                    def.cooldowns.put(sf, sv.cd());
                });
            }
        }
        for (int i = 0; i < def.hand.size(); i++){
            Card c = def.hand.get(i); final int idx = i;
            if (c.type().equals("dodge")){
                opts.add("手牌【躲闪】"); acts.add(() -> def.hand.remove(idx));
            } else if (c.type().equals("omnishield")){
                opts.add("手牌【全能盾牌】"); acts.add(() -> def.hand.remove(idx));
            }
        }
        if (opts.isEmpty()) return dmg;
        int choice = ui.chooseDefense(opts);
        if (choice == -1) return dmg;
        acts.get(choice).run();
        return 0;
    }

    boolean tryDefendDebuff(Player def, String name){
        for (int i = 0; i < def.hand.size(); i++)
            if (def.hand.get(i).type().equals("omnishield")){
                if (ui.confirm(String.format("%s 即将被【%s】！使用全能盾牌？", def.name, name))){
                    def.hand.remove(i);
                    ui.log(String.format("🛡 %s 用全能盾牌抵挡【%s】", def.name, name), "skill");
                    return true;
                }
                return false;
            }
        return false;
    }

    int playAttackCard(Player cur, Player opp, Card card, String type, int base, boolean pierce){
        int d = base;
        if (card.type().equals("attack")) d += getNormalAttackBonus(cur);
        if (cur.doubleAttackLeft > 0){ d *= 2; cur.doubleAttackLeft--; ui.log("🔥 狂击增幅翻倍","skill"); }
        if (cur.damageDoubleTurn){ d *= 2; ui.log("⚡ 翻倍","skill"); }
        d += equipmentAttackBonus(cur);
        int a = applyDamage(opp, d, cur, type, pierce);
        opp.hp -= a;
        if (a > 0){
            String tn = type.equals("physical") ? "物理" : type.equals("magic") ? "法术" : "";
            ui.log(String.format("%s 对 %s 造成 %d 点%s伤害", cur.name, opp.name, a, tn), "damage");
        }
        return a;
    }

    boolean tryRevive(Player p){
        if (p.hp > 0) return true;
        if ("秦默".equals(p.charName)){
            if (p.reviveCount >= 2){ ui.log("💀 复生献祭次数用尽","damage"); return false; }
            int cost = p.reviveCount == 0 ? 5 : 10;
            if (p.hand.size() < cost){ ui.log("💀 手牌不足 "+cost,"damage"); return false; }
            for (int i=0;i<cost;i++) p.hand.remove(p.hand.size()-1);
            p.hp = p.maxHp; p.reviveCount++;
            ui.log(String.format("✨ %s 复生献祭 (%d/2)", p.name, p.reviveCount),"skill");
            return true;
        }
        if ("多斯".equals(p.charName) && p.reviveCount < 1){
            p.hp = p.maxHp; p.reviveCount++;
            ui.log("✨ "+p.name+" 复生仪式复活","skill");
            return true;
        }
        return false;
    }

    int doDraw(Player p, int n){
        int c = 0;
        for (int i=0;i<n && !deck.isEmpty();i++) if (p.drawCard(deck.pop())) c++;
        return c;
    }

    void onTurnStart(Player p){
        if (p.cdPenalty > 0){
            for (String k : p.cooldowns.keySet())
                if (p.cooldowns.get(k) > 0) p.cooldowns.put(k, p.cooldowns.get(k) + p.cdPenalty);
            ui.log(String.format("⏱ %s 冷却CD+%d", p.name, p.cdPenalty),"skill");
            p.cdPenalty = 0;
        }
        if ("多斯".equals(p.charName)){ p.hp = Math.min(p.maxHp, p.hp+1); ui.log("✨ 愈愈光环回血","heal"); }
        if ("医者".equals(p.charName)){ p.hp = Math.min(p.maxHp, p.hp+1); ui.log("💚 医心回血","heal"); }
        if (p.hasEquip("医者药箱") && !"医者".equals(p.charName)){ p.hp = Math.min(p.maxHp, p.hp+1); ui.log("💊 医者药箱回血","heal"); }
        if (p.poisonTurns > 0){ p.hp--; p.poisonTurns--; ui.log(String.format("☠ %s 中毒 (剩%d)", p.name, p.poisonTurns),"damage"); }
        if (p.burnTurns > 0){ p.hp--; p.burnTurns--; ui.log(String.format("🔥 %s 灼烧 (剩%d)", p.name, p.burnTurns),"damage"); }
        if (p.hasEquip("能量护盾") && p.energyShieldHp == 0){ p.energyShieldHp = 1; ui.log("⚡ 能量护盾恢复","skill"); }
    }

    boolean useSkill(Player user, Player opp, String sid){
        GameData.Skill sk = user.charData.skills().get(sid);
        if (sk == null) return false;
        if (user.cooldowns.getOrDefault(sid, 0) > 0){ ui.log("【"+sk.name()+"】冷却中","system"); return false; }
        String name = sk.name();
        if (GameData.DEFENSE_SKILLS.contains(name)){ ui.log("防御技能只能在受伤时使用","system"); return false; }

        switch (name){
            case "劫掠" -> {
                if (user.hand.size() < 3){ ui.log("需要3张手牌","system"); return false; }
                user.hand.remove(user.hand.size()-1); user.hand.remove(user.hand.size()-1);
                if (!opp.equipment.isEmpty()){
                    Equipment st = opp.equipment.remove(rng.nextInt(opp.equipment.size()));
                    opp.recalcStats();
                    if (user.canEquip(st)){ user.equip(st); ui.log("🪝 抢走【"+st.name()+"】","skill"); }
                } else if (!opp.hand.isEmpty()){
                    Card st = opp.hand.remove(rng.nextInt(opp.hand.size()));
                    user.hand.add(st); ui.log("🪝 偷走手牌","skill");
                }
            }
            case "废土壁垒" -> { user.immuneTurn = true; ui.log("🛡 废土壁垒","skill"); }
            case "噬血" -> { opp.hp--; user.hp = Math.min(user.maxHp, user.hp+1); ui.log("🩸 噬血","skill"); }
            case "猎击" -> { int d = applyDamage(opp,2,user,"normal",false); opp.hp-=d; ui.log("🏹 猎击 "+d,"damage"); }
            case "腐毒侵蚀" -> {
                if (!tryDefendDebuff(opp,"腐毒侵蚀")){
                    opp.poisonTurns = 3;
                    if (opp.handLimitReduction < 1) opp.handLimitReduction = 1;
                    ui.log("☠ "+opp.name+" 中毒3回合，手牌上限-1","skill");
                }
            }
            case "速击" -> { int d = applyDamage(opp,1,user,"normal",false); opp.hp-=d; ui.log("⚡ 速击 "+d,"damage"); }
            case "坚韧蜕变" -> { user.baseMaxHp++; user.baseAtk++; user.recalcStats(); ui.log("💪 永久+1HP+1物攻","skill"); }
            case "电击眩晕","禁锢射击","锁滞" -> { opp.skipNextTurn = true; ui.log("⛓ "+opp.name+" 跳过出牌","skill"); }
            case "狂击增幅" -> { user.doubleAttackLeft = 2; ui.log("🔥 下2张攻击翻倍","skill"); }
            case "固本" -> { user.hp = Math.min(user.maxHp, user.hp+1); ui.log("💚 固本回血","heal"); }
            case "剑击" -> { int d = applyDamage(opp,2,user,"normal",false); opp.hp-=d; ui.log("⚔ 剑击 "+d,"damage"); }
            case "火焰灼烧" -> { int d = applyDamage(opp,2,user,"normal",false); opp.hp-=d; ui.log("🔥 火焰灼烧 "+d,"damage"); }
            case "突刺" -> { int d = applyDamage(opp,1,user,"normal",false); opp.hp-=d; ui.log("🗡 突刺 "+d,"damage"); }
            case "寒冰增幅" -> { user.baseAtk++; user.baseMatk++; user.recalcStats(); ui.log("❄ +1物攻+1法攻","skill"); }
            case "迟滞弹" -> {
                for (String k : opp.cooldowns.keySet())
                    if (opp.cooldowns.get(k) > 0) opp.cooldowns.put(k, opp.cooldowns.get(k)+2);
                ui.log("🔫 迟滞弹 CD+2","skill");
            }
            case "时空回溯" -> { ui.log("⏰ 回溯（简化：本次不生效）","system"); return false; }
            case "焚身灼烧" -> {
                if (!tryDefendDebuff(opp,"焚身灼烧")){
                    int t = ("帝郡".equals(user.charName) && user.hasEquip("熔岩动力戟")) ? 5 : 3;
                    opp.burnTurns = t; ui.log("🔥 灼烧"+t+"回合","skill");
                }
            }
            case "罪罚锁狱" -> { user.transferActive = true; user.transferTarget = opp; ui.log("⚖ 伤害转移","skill"); }
            case "激光" -> { int d = applyDamage(opp,1,user,"magic",false); opp.hp-=d; ui.log("🔵 激光 "+d,"damage"); }
            case "浮游炮" -> { int d = applyDamage(opp,3,user,"magic",false); opp.hp-=d; ui.log("💥 浮游炮 "+d,"damage"); }
            case "傀儡术" -> {
                if (opp.hand.isEmpty()){ ui.log("对手无手牌","system"); return false; }
                List<String> names = new ArrayList<>();
                for (Card c : opp.hand) names.add(c.name());
                int sel = ui.chooseHandCard(names,"选择借用");
                if (sel < 0) return false;
                Card st = opp.hand.get(sel);
                ui.log("🎭 借用【"+st.name()+"】","skill");
                switch (st.type()){
                    case "attack" -> { int d = applyDamage(opp, st.value()+getNormalAttackBonus(user), user, "normal", false); opp.hp -= d; }
                    case "physical" -> { int d = applyDamage(opp, opp.atk, user, "physical", false); opp.hp -= d; }
                    case "magic" -> { int d = applyDamage(opp, opp.matk, user, "magic", false); opp.hp -= d; }
                    case "heal" -> user.hp = Math.min(user.maxHp, user.hp + st.value());
                    default -> { ui.log("该牌无法傀儡","system"); return false; }
                }
            }
            case "锐击" -> { int d = applyDamage(opp,2,user,"normal",false); opp.hp-=d; ui.log("⚔ 锐击 "+d,"damage"); }
            case "拓械" -> {
                if (user.extraSlots >= 3){ ui.log("已达上限","system"); return false; }
                if (user.hand.size() < 2){ ui.log("需要2张手牌","system"); return false; }
                user.hand.remove(user.hand.size()-1); user.hand.remove(user.hand.size()-1);
                user.extraSlots++;
                ui.log(String.format("⚙ 额外装备槽 (%d/3)", user.extraSlots),"skill");
            }
            case "战威增幅" -> { user.damageDoubleTurn = true; ui.log("⚡ 本回合伤害翻倍","skill"); }
            case "愈护" -> { user.hp = Math.min(user.maxHp, user.hp+1); ui.log("💚 愈护回血","heal"); }
            case "速愈调度" -> {
                if (user.cooldowns.getOrDefault("1",0) > 0)
                    user.cooldowns.put("1", Math.max(0, user.cooldowns.get("1")-2));
                ui.log("⏩ 一技能CD-2","skill");
            }
            case "虚空爆裂" -> { int d = applyDamage(opp,2,user,"magic",false); opp.hp-=d; user.hp = Math.min(user.maxHp, user.hp+1); ui.log("🌌 虚空爆裂 "+d,"damage"); }
            case "虚无缠绕" -> { opp.handLimitReduction += 2; ui.log("🌫 "+opp.name+" 手牌上限-2","skill"); }
            case "湮灭" -> { int d = applyDamage(opp,3,user,"magic",false); opp.hp-=d; ui.log("💀 湮灭 "+d,"damage"); }
            case "冲锋" -> { int d = applyDamage(opp,2,user,"physical",false); opp.hp-=d; ui.log("🐎 冲锋 "+d,"damage"); }
            case "铁壁" -> { user.energyShieldHp += 2; ui.log("🛡 铁壁 +2护盾","skill"); }
            case "势不可挡" -> { user.damageDoubleTurn = true; ui.log("🔥 势不可挡","skill"); }
            case "连射" -> { int d = applyDamage(opp,1,user,"normal",false); opp.hp-=d; user.doubleAttackLeft++; ui.log("🏹 连射 "+d,"damage"); }
            case "致命射击" -> { int d = applyDamage(opp,3,user,"physical",false); opp.hp-=d; ui.log("🎯 致命射击 "+d,"damage"); }
            case "猎鹰之眼" -> { user.damageDoubleTurn = true; ui.log("🦅 猎鹰之眼","skill"); }
            case "圣光" -> { user.hp = Math.min(user.maxHp, user.hp+2); ui.log("✨ 圣光 +2","heal"); }
            case "净化" -> { user.poisonTurns = 0; user.burnTurns = 0; ui.log("💧 净化","skill"); }
            case "神恩" -> { user.hp = Math.min(user.maxHp, user.hp+3); user.energyShieldHp += 2; ui.log("👼 神恩","skill"); }
            case "影袭" -> { int d = applyDamage(opp,2,user,"physical",false); opp.hp-=d; user.dodgeTokens++; ui.log("🌑 影袭 "+d,"damage"); }
            case "分身术" -> { user.dodgeTokens += 2; ui.log("🌫 分身术 +2闪避","skill"); }
            case "致命偷袭" -> { int d = applyDamage(opp,3,user,"physical",true); opp.hp-=d; ui.log("🗡 致命偷袭 "+d,"damage"); }
            case "神圣壁垒" -> { user.energyShieldHp += 2; user.hp = Math.min(user.maxHp, user.hp+1); ui.log("🛡 神圣壁垒","skill"); }
            case "审判" -> { int d = applyDamage(opp,1,user,"magic",false); opp.hp-=d; user.hp = Math.min(user.maxHp, user.hp+1); ui.log("⚖ 审判 "+d,"damage"); }
            case "圣光庇护" -> { user.energyShieldHp += 2; user.immuneTurn = true; ui.log("🌟 圣光庇护","skill"); }
            case "烈焰冲击" -> { int d = applyDamage(opp,2,user,"magic",false); opp.hp-=d; opp.burnTurns = Math.max(opp.burnTurns,2); ui.log("🔥 烈焰冲击 "+d,"damage"); }
            case "寒冰锁链" -> { int d = applyDamage(opp,1,user,"magic",false); opp.hp-=d; opp.skipNextTurn = true; ui.log("❄ 寒冰锁链 "+d,"skill"); }
            case "雷霆万钧" -> { int d = applyDamage(opp,3,user,"magic",false); opp.hp-=d; ui.log("⚡ 雷霆万钧 "+d,"damage"); }
            case "标记猎杀" -> { opp.cdPenalty += 1; ui.log("🎯 "+opp.name+" 被标记","skill"); }
            case "陷阱" -> { opp.hp -= 2; ui.log("🪤 陷阱 2点","skill"); }
            case "致命枪决" -> { int d = opp.hp < 3 ? 6 : 1; int a = applyDamage(opp,d,user,"physical",false); opp.hp-=a; ui.log("💥 致命枪决 "+a,"damage"); }
            case "龙息" -> { int d = applyDamage(opp,2,user,"magic",false); opp.hp-=d; ui.log("🐉 龙息 "+d,"damage"); }
            case "龙鳞护体" -> { user.energyShieldHp += 2; user.dodgeTokens++; ui.log("🛡 龙鳞护体","skill"); }
            case "龙威" -> { user.damageDoubleTurn = true; ui.log("🐲 龙威","skill"); }
            case "群体治疗" -> { user.hp = Math.min(user.maxHp, user.hp+1); ui.log("💚 群体治疗 +1","heal"); }
            case "净化之光" -> { user.poisonTurns = 0; user.burnTurns = 0; ui.log("💧 净化之光","skill"); }
            case "生命链接" -> { user.hp = Math.min(user.maxHp, user.hp+2); ui.log("🔗 生命链接 +2","heal"); }
            case "狂暴突袭" -> { int d = applyDamage(opp,3,user,"physical",false); opp.hp-=d; user.hp -= 1; ui.log("💢 狂暴突袭 "+d,"damage"); }
            case "血怒" -> { user.hp -= 3; user.damageDoubleTurn = true; ui.log("🩸 血怒","skill"); }
            case "绝命狂斩" -> { int loss = user.maxHp - user.hp; int d = applyDamage(opp,loss,user,"physical",false); opp.hp-=d; user.hp = Math.min(user.maxHp, user.hp+2); ui.log("⚔ 绝命狂斩 "+d,"damage"); }
            case "火焰符" -> { int d = applyDamage(opp,1,user,"magic",false); opp.hp-=d; opp.burnTurns = Math.max(opp.burnTurns,1); ui.log("🔥 火焰符 "+d,"damage"); }
            case "冰封符" -> { int d = applyDamage(opp,1,user,"magic",false); opp.hp-=d; opp.skipNextTurn = true; ui.log("❄ 冰封符 "+d,"skill"); }
            case "万象符阵" -> { int d = applyDamage(opp,2,user,"magic",false); opp.hp-=d; int dr = doDraw(user,2); ui.log("🌀 万象符阵 "+d+" 抽"+dr,"damage"); }
            case "剧毒喷射" -> { int d = applyDamage(opp,1,user,"magic",false); opp.hp-=d; opp.poisonTurns = 3; ui.log("☠ 剧毒喷射 "+d,"damage"); }
            case "虚弱诅咒" -> { opp.cdPenalty += 2; ui.log("🌫 "+opp.name+" 虚弱","skill"); }
            case "万毒噬心" -> { int d = applyDamage(opp,2,user,"magic",false); opp.hp-=d; opp.poisonTurns = 3; ui.log("💀 万毒噬心 "+d,"damage"); }
            case "挑战" -> { int d = applyDamage(opp,2,user,"physical",false); opp.hp-=d; user.dodgeTokens++; ui.log("⚔ 挑战 "+d,"damage"); }
            case "格挡反击" -> { user.energyShieldHp += 2; ui.log("🛡 格挡反击","skill"); }
            case "终结一击" -> { int d = applyDamage(opp,5,user,"physical",false); opp.hp-=d; user.hp -= 2; ui.log("💥 终结一击 "+d,"damage"); }
            case "流星" -> { int d = applyDamage(opp,3,user,"magic",false); opp.hp-=d; ui.log("☄ 流星 "+d,"damage"); }
            case "命运干预" -> { int dr = doDraw(user,2); ui.log("🔮 命运干预 抽"+dr,"info"); }
            case "星辰陨落" -> { int d = applyDamage(opp,4,user,"magic",false); opp.hp-=d; user.cdPenalty = 1; ui.log("🌠 星辰陨落 "+d,"damage"); }
            case "连环拳" -> { int d = applyDamage(opp,1,user,"physical",false); opp.hp-=d; user.extraAttackTokens++; ui.log("👊 连环拳 "+d,"damage"); }
            case "金刚护体" -> { user.energyShieldHp += 2; user.hp = Math.min(user.maxHp, user.hp+1); ui.log("🙏 金刚护体","skill"); }
            case "天崩地裂" -> { int d = applyDamage(opp,4,user,"physical",false); opp.hp-=d; opp.cdPenalty = 1; ui.log("🌋 天崩地裂 "+d,"damage"); }
            case "复生献祭","复生仪式" -> { ui.log("阵亡时自动触发","system"); return false; }
            default -> { ui.log("技能未实现："+name,"system"); return false; }
        }
        int cd = sk.cd();
        if (name.equals("激光") && "冷锋".equals(user.charName) && user.hasEquip("脉冲炮")) cd = Math.max(0,cd-1);
        if (name.equals("愈护") && "多斯".equals(user.charName) && user.hasEquip("医疗包")) cd = Math.max(0,cd-2);
        if (name.equals("废土壁垒") && "拾荒者".equals(user.charName) && user.hasEquip("烟雾掩护")) cd = Math.max(0,cd-1);
        if (cd > 0) user.cooldowns.put(sid, cd);
        return true;
    }

    void useCard(int idx){
        if (over) return;
        Card card = current.hand.get(idx);
        String t = card.type();
        Player cur = current, opp = opponent;

        if (t.equals("attack")||t.equals("physical")||t.equals("magic")){
            int limit = getAttackLimit(cur);
            boolean extra = false;
            if (cur.extraAttackTokens > 0 && t.equals("attack") && countAttacks >= limit){
                cur.extraAttackTokens--; extra = true;
                ui.log("🔄 使用连击的额外普攻","skill");
            }
            if (!extra && countAttacks >= limit && limit < 999){
                ui.log("本回合攻击上限 "+limit,"system"); return;
            }
            boolean pierce = "pierce".equals(card.effect());
            switch (t){
                case "attack" -> playAttackCard(cur,opp,card,"normal",card.value(),pierce);
                case "physical" -> playAttackCard(cur,opp,card,"physical",cur.atk,false);
                case "magic" -> playAttackCard(cur,opp,card,"magic",cur.matk,false);
            }
            if ("combo".equals(card.effect())){ cur.extraAttackTokens++; ui.log("🔄 连击 +1 额外普攻","skill"); }
            else if ("quick".equals(card.effect())){ cur.dodgeTokens++; ui.log("💨 快刺 +1 闪避","skill"); }
            else if ("bind".equals(card.effect())){ opp.cdPenalty++; ui.log("⏱ 牵制 对手CD+1","skill"); }
            else if ("blood".equals(card.effect())){ cur.hp -= 1; ui.log("🩸 浴血自伤1","damage"); }
            cur.hand.remove(idx); countAttacks++; cur.attackedThisTurn = true;
        }
        else if (t.equals("heal")){ cur.hp = Math.min(cur.maxHp, cur.hp+card.value()); ui.log("💚 回复 "+card.value(),"heal"); cur.hand.remove(idx); }
        else if (t.equals("rage")){ cur.damageDoubleTurn = true; ui.log("🔥 狂暴剂","skill"); cur.hand.remove(idx); }
        else if (t.equals("anesthetic")){
            if (opp.skipFullTurn){ ui.log("目标已被麻醉","system"); return; }
            opp.skipFullTurn = true; ui.log("💉 麻醉剂","skill"); cur.hand.remove(idx);
        }
        else if (t.equals("meditate")){
            if (cur.meditateUsedThisTurn){ ui.log("已用过沉思","system"); return; }
            cur.meditateUsedThisTurn = true;
            cur.hand.remove(idx);
            int d = doDraw(cur, 5);
            ui.log("📖 沉思抽 "+d,"info");
        }
        else if (t.equals("dismantle")){
            if (opp.equipment.isEmpty()){ ui.log("对手无装备","system"); return; }
            List<String> names = new ArrayList<>();
            for (Equipment e : opp.equipment) names.add(e.name()+"（"+e.type()+"）");
            int sel = ui.chooseEquipment(names, "选择要拆除");
            if (sel < 0) return;
            Equipment r = opp.equipment.remove(sel); opp.recalcStats();
            ui.log("🔨 拆除【"+r.name()+"】","skill");
            cur.hand.remove(idx);
        }
        else if (t.equals("steal")){
            if (opp.equipment.isEmpty()){ ui.log("对手无装备","system"); return; }
            List<String> names = new ArrayList<>();
            for (Equipment e : opp.equipment) names.add(e.name()+"（"+e.type()+"）");
            int sel = ui.chooseEquipment(names, "选择要抢夺");
            if (sel < 0) return;
            Equipment eq = opp.equipment.get(sel);
            if (!cur.canEquip(eq)){ ui.log("装备栏已满","system"); return; }
            opp.equipment.remove(sel); opp.recalcStats();
            cur.equip(eq);
            if ("能量护盾".equals(eq.name())) cur.energyShieldHp = 1;
            ui.log("🎯 抢夺【"+eq.name()+"】","skill");
            cur.hand.remove(idx);
        }
        else if (t.equals("duel")){ ui.log("⚔ 决斗（简化）","skill"); opp.hp -= 2; cur.hp -= 2; cur.hand.remove(idx); }
        else if (t.equals("equip")){
            Equipment eq = Equipment.fromName(card.name());
            if (!cur.canEquip(eq)){ ui.log("装备栏已满","system"); return; }
            cur.hand.remove(idx); cur.equip(eq);
            if ("能量护盾".equals(eq.name())) cur.energyShieldHp = 1;
            ui.log("⚙ 装备【"+eq.name()+"】","info");
        }
        else if (t.equals("shieldcard")){ cur.energyShieldHp++; ui.log("🛡 +1护盾","skill"); cur.hand.remove(idx); }
        else if (t.equals("cleanse")){ cur.poisonTurns = 0; cur.burnTurns = 0; ui.log("💧 净化","skill"); cur.hand.remove(idx); }
        else if (t.equals("chaos")){ int d = 1+rng.nextInt(3); int a = applyDamage(opp,d,cur,"normal",false); opp.hp-=a; ui.log("🎲 乱击 "+a,"damage"); cur.hand.remove(idx); }
        else if (t.equals("desperate")){ int d = Math.max(1,cur.maxHp-cur.hp); int a = applyDamage(opp,d,cur,"normal",false); opp.hp-=a; ui.log("💥 绝境 "+a,"damage"); cur.hand.remove(idx); }
        else if (t.equals("sacrifice")){ cur.hp -= 1; cur.hand.remove(idx); int d = doDraw(cur,3); ui.log("🩸 血祭 -1HP 抽"+d,"damage"); }
        else if (t.equals("confuse")){
            if (!opp.hand.isEmpty()){
                int di = rng.nextInt(opp.hand.size());
                Card dc = opp.hand.remove(di);
                ui.log("🌀 "+opp.name+" 弃【"+dc.name()+"】","skill");
            }
            cur.hand.remove(idx);
        }
        else if (t.equals("ragepotion")){ cur.damageDoubleTurn = true; ui.log("🍷 狂暴药水","skill"); cur.hand.remove(idx); }
        else if (t.equals("freeze")){ opp.handLimitReduction++; ui.log("🧊 急冻","skill"); cur.hand.remove(idx); }
        else if (t.equals("dodge")||t.equals("omnishield")){ ui.log("这张牌只能在受伤时用","system"); return; }

        if (!opp.isAlive()){ if (!tryRevive(opp)){ endGame(); return; } }
        ui.refresh();
    }

    void endTurn(){
        if (over) return;
        current.reduceCooldowns();
        current.onTurnEnd();
        countAttacks = 0;
        Player tmp = current; current = opponent; opponent = tmp;
        turn++;
        startTurn();
    }

    void start(){ startTurn(); }

    void startTurn(){
        if (over) return;
        Player cur = current;
        ui.log("", "system");
        ui.log(String.format("--- 第 %d 回合 · %s ---", turn, cur.name),"info");
        cur.resetTurnFlags();
        onTurnStart(cur);
        if (!cur.isAlive()){ if (tryRevive(cur)){ ui.refresh(); return; } endGame(); return; }

        boolean skipped = cur.skipNextTurn;
        int cnt = cur.hand.isEmpty() ? 3 : (skipped ? 1 : 2);
        if (cur.hasEquip("符卡册") || "符卡师".equals(cur.charName)) cnt++;
        if (!deck.isEmpty()){
            int d = doDraw(cur, cnt);
            ui.log(String.format("%s 摸了 %d 张牌", cur.name, d),"info");
        }
        if (cur.skipNextTurn){
            ui.log("⛔ "+cur.name+" 跳过出牌","system");
            cur.skipNextTurn = false; ui.refresh(); endTurn(); return;
        }
        ui.refresh();
        if (aiPlayerIndex == (current == p1 ? 1 : 2)){
            Platform.runLater(() -> AIPlayer.takeTurn(this, ui));
        }
    }

    void endGame(){
        over = true;
        Player winner = p1.isAlive() ? p1 : p2;
        ui.log("", "system");
        ui.log("🏆 "+winner.name+" 获胜！","skill");
        if (winner == p1) RankingSystem.recordWin(); else RankingSystem.recordLoss();

        BattleStats s = new BattleStats();
        s.timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        s.playerChar = p1.charName; s.aiChar = p2.charName;
        s.win = winner == p1; s.turns = turn;
        BattleStats.record(s);
        ui.refresh();
    }
}

// ============================================================
// AI
// ============================================================
class AIPlayer {
    static void takeTurn(BattleEngine e, BattleUI ui){
        if (e.over) return;
        Player me = e.current, opp = e.opponent;
        new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                try {
                    if (me.hp <= 2){
                        for (String sid : me.charData.skills().keySet()){
                            String skn = me.charData.skills().get(sid).name();
                            if ((skn.equals("愈护")||skn.equals("固本")||skn.equals("圣光")||
                                    skn.equals("群体治疗")||skn.equals("神圣壁垒"))
                                    && me.cooldowns.getOrDefault(sid,0) == 0){
                                e.useSkill(me, opp, sid); break;
                            }
                        }
                    } else {
                        for (String sid : me.charData.skills().keySet()){
                            String skn = me.charData.skills().get(sid).name();
                            if (!GameData.DEFENSE_SKILLS.contains(skn)
                                    && !skn.equals("复生献祭") && !skn.equals("复生仪式")
                                    && me.cooldowns.getOrDefault(sid,0) == 0){
                                e.useSkill(me, opp, sid); break;
                            }
                        }
                    }
                    while (!e.over && e.current == me){
                        int idx = pickCard(me, opp);
                        if (idx < 0) break;
                        e.useCard(idx);
                        Thread.sleep(200);
                    }
                    if (!e.over && e.current == me) e.endTurn();
                } catch (Exception ex){ ex.printStackTrace(); }
            });
        }).start();
    }

    static int pickCard(Player me, Player opp){
        int bAtk=-1, bDmg=-1, bHeal=-1;
        for (int i=0;i<me.hand.size();i++){
            Card c = me.hand.get(i);
            if (c.type().equals("attack")||c.type().equals("physical")||c.type().equals("magic")){
                int d = c.type().equals("attack")?c.value():(c.type().equals("physical")?me.atk:me.matk);
                if (d > bDmg){ bDmg = d; bAtk = i; }
            } else if (c.type().equals("heal") && me.hp < me.maxHp){
                if (bHeal < 0 || c.value() > me.hand.get(bHeal).value()) bHeal = i;
            }
        }
        if (opp.hp <= 3 && bAtk >= 0) return bAtk;
        if (me.hp <= 2 && bHeal >= 0) return bHeal;
        for (int i=0;i<me.hand.size();i++) if (me.hand.get(i).type().equals("equip")) return i;
        if (bAtk >= 0) return bAtk;
        if (bHeal >= 0) return bHeal;
        return -1;
    }
}

// ============================================================
// 战斗 UI
// ============================================================
class BattleUI {
    private final BorderPane root = new BorderPane();
    private VBox panelP1, panelP2, logBox, handBox;
    private ScrollPane logScroll;
    private Label turnLabel, handTitle;
    private Button btnEndTurn, btnSkill;
    private BattleEngine engine;

    BattleUI(){
        root.setStyle(GameData.BG_GRAD);
        root.setPadding(new Insets(8));

        HBox topbar = new HBox(20);
        topbar.setAlignment(Pos.CENTER_LEFT);
        topbar.setPadding(new Insets(8));
        topbar.setStyle("-fx-background-color:#222;-fx-background-radius:8;");
        turnLabel = new Label("第 1 回合");
        turnLabel.setFont(Font.font("System", FontWeight.BOLD, 17));
        turnLabel.setTextFill(Color.web("#ffd93d"));
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label ver = new Label(GameApp.FULL_TITLE);
        ver.setTextFill(Color.web("#666")); ver.setFont(Font.font(11));
        topbar.getChildren().addAll(turnLabel, sp, ver);

        panelP1 = new VBox(6); panelP2 = new VBox(6);
        HBox players = new HBox(8, panelP1, panelP2);
        HBox.setHgrow(panelP1, Priority.ALWAYS);
        HBox.setHgrow(panelP2, Priority.ALWAYS);

        logBox = new VBox(2);
        logBox.setPadding(new Insets(8));
        logBox.setStyle("-fx-background-color:#111;-fx-background-radius:8;");
        logScroll = new ScrollPane(logBox);
        logScroll.setFitToWidth(true);
        logScroll.setPrefHeight(200);
        logScroll.setStyle("-fx-background:#111;");
        VBox.setVgrow(logScroll, Priority.ALWAYS);

        VBox center = new VBox(8, players, logScroll);
        VBox.setVgrow(center, Priority.ALWAYS);

        handTitle = new Label("手牌");
        handTitle.setTextFill(Color.web("#ffd93d"));
        handTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        handBox = new VBox(6);
        ScrollPane handScroll = new ScrollPane(handBox);
        handScroll.setFitToWidth(true);
        handScroll.setPrefHeight(150);
        handScroll.setStyle("-fx-background:#222;");

        btnSkill = new Button("技能");
        btnSkill.setStyle("-fx-background-color:#3a5;-fx-text-fill:white;-fx-padding:8 16;");
        btnEndTurn = new Button("结束回合");
        btnEndTurn.setStyle("-fx-background-color:#555;-fx-text-fill:white;-fx-padding:8 16;");

        HBox btnRow = new HBox(8, btnSkill, btnEndTurn);
        VBox handPane = new VBox(6, handTitle, handScroll, btnRow);
        handPane.setPadding(new Insets(8));
        handPane.setStyle("-fx-background-color:#222;-fx-background-radius:8;");

        root.setTop(topbar);
        root.setCenter(center);
        root.setBottom(handPane);

        root.setFocusTraversable(true);
        root.setOnKeyPressed(e -> {
            if (engine == null || engine.over) return;
            if (e.getCode() == javafx.scene.input.KeyCode.SPACE) engine.endTurn();
            else if (e.getCode() == javafx.scene.input.KeyCode.S) showSkillMenu();
        });
        Platform.runLater(root::requestFocus);

        btnEndTurn.setOnAction(e -> { if (engine != null && !engine.over) engine.endTurn(); });
        btnSkill.setOnAction(e -> showSkillMenu());
    }

    Region getRoot(){ return root; }
    void setEngine(BattleEngine e){ this.engine = e; }

    void log(String msg, String cls){
        Label l = new Label(msg.isEmpty() ? " " : msg);
        l.setFont(Font.font("Consolas", 12));
        l.setWrapText(true);
        String color = switch (cls) {
            case "damage" -> "#ff6b6b";
            case "heal" -> "#6bff8a";
            case "skill" -> "#ffd93d";
            case "system" -> "#888";
            default -> "#7ec8e3";
        };
        l.setTextFill(Color.web(color));
        logBox.getChildren().add(l);
        Platform.runLater(() -> logScroll.setVvalue(1.0));
    }

    void refresh(){
        if (engine == null) return;
        renderPanel(engine.p1, panelP1, engine.current == engine.p1);
        renderPanel(engine.p2, panelP2, engine.current == engine.p2);
        turnLabel.setText(engine.over ? "游戏结束"
                : String.format("第 %d 回合 · %s", engine.turn, engine.current.name));
        renderHand();
    }

    private void renderPanel(Player p, VBox box, boolean active){
        box.getChildren().clear();
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color:#222;-fx-background-radius:10;-fx-border-width:2;" +
                "-fx-border-radius:10;-fx-border-color:" + (active ? "#ffd93d" : "#444") + ";");
        Label n = new Label(p.name + " · " + p.charName + (active ? "  ◀" : ""));
        n.setFont(Font.font("System", FontWeight.BOLD, 15));
        n.setTextFill(Color.web("#ffd93d"));
        ProgressBar hpBar = new ProgressBar(Math.max(0, (double)p.hp/p.maxHp));
        hpBar.setPrefWidth(Double.MAX_VALUE);
        hpBar.setStyle("-fx-accent:#e74c3c;");
        Label hpTxt = new Label(String.format("❤ %d/%d", p.hp, p.maxHp));
        hpTxt.setTextFill(Color.web("#eee"));
        Label st = new Label(String.format("⚔%d 🔮%d 🎴%d/%d", p.atk, p.matk, p.hand.size(), p.getHandLimit()));
        st.setTextFill(Color.web("#ccc"));
        StringBuilder eq = new StringBuilder();
        for (Equipment e : p.equipment) eq.append("[").append(e.name()).append("] ");
        Label eqL = new Label(eq.isEmpty() ? "（无装备）" : eq.toString());
        eqL.setTextFill(Color.web("#aaa")); eqL.setWrapText(true); eqL.setFont(Font.font(11));
        StringBuilder stt = new StringBuilder();
        if (p.poisonTurns > 0) stt.append("☠").append(p.poisonTurns).append(" ");
        if (p.burnTurns > 0) stt.append("🔥").append(p.burnTurns).append(" ");
        if (p.immuneTurn) stt.append("✨ ");
        if (p.dodgeTokens > 0) stt.append("💨×").append(p.dodgeTokens).append(" ");
        if (p.energyShieldHp > 0) stt.append("🛡").append(p.energyShieldHp).append(" ");
        if (p.doubleAttackLeft > 0) stt.append("⚡×").append(p.doubleAttackLeft).append(" ");
        if (p.damageDoubleTurn) stt.append("💥 ");
        if (p.skipNextTurn) stt.append("⛓ ");
        Label sttL = new Label(stt.toString());
        sttL.setTextFill(Color.web("#f99")); sttL.setWrapText(true);
        box.getChildren().addAll(n, hpBar, hpTxt, st, eqL, sttL);
    }

    private void renderHand(){
        handBox.getChildren().clear();
        if (engine.over) return;
        Player cur = engine.current;
        handTitle.setText(String.format("%s 的手牌 (%d)", cur.name, cur.hand.size()));
        HBox row = new HBox(8);
        row.setPadding(new Insets(4));
        for (int i=0;i<cur.hand.size();i++){
            final int idx = i;
            Card c = cur.hand.get(i);
            Button b = new Button(c.name() + "\n" + shortType(c.type()));
            b.setWrapText(true);
            b.setPrefSize(130, 90);
            b.setStyle(cardStyle(c.type()));
            b.setOnAction(e -> engine.useCard(idx));
            row.getChildren().add(b);
        }
        if (cur.hand.isEmpty()){
            Label empty = new Label("（无手牌）");
            empty.setTextFill(Color.web("#666"));
            row.getChildren().add(empty);
        }
        handBox.getChildren().add(row);
        btnEndTurn.setDisable(engine.over || cur.skipNextTurn);
        btnSkill.setDisable(engine.over);
    }

    private String shortType(String t){
        return switch (t){
            case "attack" -> "普攻"; case "physical" -> "物理"; case "magic" -> "法术";
            case "heal" -> "治疗"; case "dodge" -> "躲闪"; case "omnishield" -> "全能盾";
            case "rage" -> "狂暴"; case "anesthetic" -> "麻醉"; case "meditate" -> "沉思";
            case "dismantle" -> "拆除"; case "steal" -> "抢夺"; case "duel" -> "决斗";
            case "equip" -> "装备"; case "shieldcard" -> "护盾卡"; case "cleanse" -> "净化";
            case "revive" -> "复生"; case "chaos" -> "乱击"; case "desperate" -> "绝境";
            case "sacrifice" -> "血祭"; case "confuse" -> "乱心";
            case "ragepotion" -> "狂暴药"; case "freeze" -> "急冻";
            default -> t;
        };
    }

    private String cardStyle(String type){
        String c = switch (type){
            case "attack" -> "#c73"; case "physical" -> "#a86"; case "magic" -> "#69c";
            case "heal" -> "#3c7"; case "dodge" -> "#39c"; case "omnishield" -> "#3c9";
            case "equip" -> "#93c"; case "shieldcard","cleanse","revive" -> "#3c9";
            case "chaos","desperate","sacrifice" -> "#933";
            default -> "#888";
        };
        return "-fx-background-color:#333;-fx-text-fill:white;-fx-border-color:" + c +
                ";-fx-border-width:2;-fx-border-radius:8;-fx-background-radius:8;-fx-cursor:hand;";
    }

    void showSkillMenu(){
        if (engine == null || engine.over) return;
        Player cur = engine.current;
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("技能");
        VBox box = new VBox(6);
        box.setPadding(new Insets(16));
        box.setStyle("-fx-background-color:#222;");
        box.getChildren().add(new Label(cur.name + " 的技能"));
        for (var e : cur.charData.skills().entrySet()){
            String sid = e.getKey(); GameData.Skill sk = e.getValue();
            int cd = cur.cooldowns.getOrDefault(sid, 0);
            Button b = new Button(sk.name() + (cd > 0 ? "  (CD " + cd + ")" : "  (CD " + sk.cd() + ")"));
            b.setMaxWidth(Double.MAX_VALUE);
            b.setStyle("-fx-background-color:#333;-fx-text-fill:white;-fx-padding:8;");
            b.setDisable(cd > 0);
            b.setOnAction(ev -> {
                boolean ok = engine.useSkill(cur, engine.opponent, sid);
                if (ok && !engine.opponent.isAlive()){
                    if (!engine.tryRevive(engine.opponent)){ engine.endGame(); dlg.close(); return; }
                }
                refresh(); dlg.close();
            });
            box.getChildren().add(b);
            Label d = new Label("  " + sk.desc());
            d.setTextFill(Color.web("#aaa")); d.setFont(Font.font(11)); d.setWrapText(true);
            box.getChildren().add(d);
        }
        dlg.getDialogPane().setContent(box);
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setStyle("-fx-background-color:#222;");
        dlg.showAndWait();
    }

    int chooseDefense(List<String> options){
        ChoiceDialog<String> dlg = new ChoiceDialog<>(options.get(0), options);
        dlg.setTitle("选择防御");
        dlg.setHeaderText("受到伤害！选择防御方式");
        dlg.setContentText("选择：");
        var r = dlg.showAndWait();
        if (r.isEmpty()) return -1;
        return options.indexOf(r.get());
    }

    int chooseEquipment(List<String> names, String title){
        ChoiceDialog<String> dlg = new ChoiceDialog<>(names.get(0), names);
        dlg.setTitle(title);
        dlg.setHeaderText(title);
        var r = dlg.showAndWait();
        if (r.isEmpty()) return -1;
        return names.indexOf(r.get());
    }

    int chooseHandCard(List<String> names, String title){
        ChoiceDialog<String> dlg = new ChoiceDialog<>(names.get(0), names);
        dlg.setTitle(title);
        dlg.setHeaderText(title);
        var r = dlg.showAndWait();
        if (r.isEmpty()) return -1;
        return names.indexOf(r.get());
    }

    boolean confirm(String msg){
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}

// ============================================================
// 图鉴
// ============================================================
class CharDex {
    public static void show(Stage owner){
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("📖 图鉴");

        BorderPane root = new BorderPane();
        root.setStyle(GameData.BG_GRAD);
        root.setPadding(new Insets(16));

        Label title = new Label("📖 图鉴");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#ffd93d"));

        TextField search = new TextField();
        search.setPromptText("🔍 搜索角色 / 装备 / 卡牌...");
        search.setStyle("-fx-background-color: rgba(255,255,255,0.1);-fx-text-fill: white;" +
                "-fx-prompt-text-fill: #888;-fx-background-radius: 8;");

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        FlowPane charGrid = new FlowPane(8, 8);
        FlowPane equipGrid = new FlowPane(8, 8);
        FlowPane cardGrid = new FlowPane(8, 8);
        charGrid.setPadding(new Insets(10));
        equipGrid.setPadding(new Insets(10));
        cardGrid.setPadding(new Insets(10));

        ScrollPane cs = new ScrollPane(charGrid);
        ScrollPane es = new ScrollPane(equipGrid);
        ScrollPane ks = new ScrollPane(cardGrid);
        cs.setFitToWidth(true); es.setFitToWidth(true); ks.setFitToWidth(true);
        cs.setStyle("-fx-background: transparent;");
        es.setStyle("-fx-background: transparent;");
        ks.setStyle("-fx-background: transparent;");

        tabs.getTabs().addAll(
                new Tab("角色 ("+GameData.CHARS.size()+")", cs),
                new Tab("装备 ("+GameData.EQUIPS.size()+")", es),
                new Tab("卡牌 ("+CardPool.ALL.size()+")", ks)
        );

        Runnable rebuild = () -> {
            String q = search.getText().trim().toLowerCase();
            charGrid.getChildren().clear(); equipGrid.getChildren().clear(); cardGrid.getChildren().clear();
            for (var e : GameData.CHARS.entrySet()){
                if (!q.isEmpty() && !e.getKey().toLowerCase().contains(q)
                        && !e.getValue().desc().toLowerCase().contains(q)) continue;
                charGrid.getChildren().add(makeCharCard(e.getKey(), e.getValue()));
            }
            for (var e : GameData.EQUIPS.entrySet()){
                if (!q.isEmpty() && !e.getKey().toLowerCase().contains(q)
                        && !e.getValue().desc().toLowerCase().contains(q)) continue;
                equipGrid.getChildren().add(makeEquipCard(e.getKey(), e.getValue()));
            }
            for (Card c : CardPool.ALL){
                if (!q.isEmpty() && !c.name().toLowerCase().contains(q)
                        && !c.desc().toLowerCase().contains(q)) continue;
                cardGrid.getChildren().add(makeCardCard(c));
            }
        };
        search.textProperty().addListener((o,a,b) -> rebuild.run());
        rebuild.run();

        root.setTop(new VBox(10, title, search));
        root.setCenter(tabs);
        stage.setScene(new Scene(root, 1000, 700));
        stage.show();
    }

    private static VBox makeCharCard(String name, GameData.Char cd){
        VBox box = new VBox(4);
        box.setPrefWidth(220); box.setPadding(new Insets(12));
        box.setStyle(GameData.CARD_BG);
        Label n = new Label(name);
        n.setFont(Font.font("System", FontWeight.BOLD, 16));
        n.setTextFill(Color.web("#ffd93d"));
        Label st = new Label(String.format("❤%d ⚔%d 🔮%d", cd.hp(), cd.atk(), cd.matk()));
        st.setTextFill(Color.web("#aaa"));
        Label d = new Label(cd.desc());
        d.setTextFill(Color.web("#bbb")); d.setWrapText(true);
        Label p = new Label("被动：" + cd.passive());
        p.setTextFill(Color.web("#7ec8e3")); p.setWrapText(true); p.setFont(Font.font(11));
        VBox sk = new VBox(2);
        for (var s : cd.skills().values()){
            Label sl = new Label("· " + s.name() + " [CD"+s.cd()+"]");
            sl.setTextFill(Color.web("#7ec8e3")); sl.setFont(Font.font(11));
            Label sd = new Label("  " + s.desc());
            sd.setTextFill(Color.web("#999")); sd.setFont(Font.font(10)); sd.setWrapText(true);
            sk.getChildren().addAll(sl, sd);
        }
        box.getChildren().addAll(n, st, d, p, sk);
        return box;
    }

    private static VBox makeEquipCard(String name, GameData.Equip ed){
        VBox box = new VBox(3);
        box.setPrefWidth(220); box.setPadding(new Insets(12));
        box.setStyle(GameData.CARD_BG);
        Label n = new Label(name);
        n.setFont(Font.font("System", FontWeight.BOLD, 15));
        n.setTextFill(Color.web("#ffd93d"));
        Label t = new Label("类型：" + ed.type()
                + (ed.owner() != null ? " · 专属：" + ed.owner() : " · 通用"));
        t.setTextFill(Color.web("#aaa")); t.setFont(Font.font(11));
        Label d = new Label(ed.desc());
        d.setTextFill(Color.web("#bbb")); d.setWrapText(true);
        box.getChildren().addAll(n, t, d);
        return box;
    }

    private static VBox makeCardCard(Card c){
        VBox box = new VBox(3);
        box.setPrefWidth(180); box.setPadding(new Insets(10));
        box.setStyle(GameData.CARD_BG);
        Label n = new Label(c.name());
        n.setFont(Font.font("System", FontWeight.BOLD, 13));
        n.setTextFill(Color.web("#ffd93d"));
        Label t = new Label(c.type());
        t.setTextFill(Color.web("#7ec8e3")); t.setFont(Font.font(11));
        Label d = new Label(c.desc());
        d.setTextFill(Color.web("#bbb")); d.setWrapText(true); d.setFont(Font.font(11));
        box.getChildren().addAll(n, t, d);
        return box;
    }
}

// ============================================================
// 统计
// ============================================================
class BattleStats implements Serializable {
    public String timestamp, playerChar, aiChar, envId = "clear", difficulty = "normal";
    public boolean win;
    public int turns, damageDealt, damageTaken, healingDone, skillUses, cardPlays, maxSingleHit;

    private static final String FILE = "battle_stats.dat";
    private static List<BattleStats> HISTORY = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static void load(){
        File f = new File(FILE);
        if (!f.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))){
            HISTORY = (List<BattleStats>) ois.readObject();
        } catch (Exception ignored) {}
    }
    public static void save(){
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE))){
            oos.writeObject(HISTORY);
        } catch (Exception ignored) {}
    }
    public static void record(BattleStats s){ HISTORY.add(s); save(); }

    public static void showHistory(Stage owner){
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("📊 对战统计");

        VBox root = new VBox(10);
        root.setStyle(GameData.BG_GRAD);
        root.setPadding(new Insets(16));

        Label title = new Label("📊 对战统计");
        title.setFont(Font.font("System", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#ffd93d"));

        int total = HISTORY.size();
        long wins = HISTORY.stream().filter(s -> s.win).count();
        double wr = total == 0 ? 0 : wins * 100.0 / total;
        int td = HISTORY.stream().mapToInt(s -> s.damageDealt).sum();
        int th = HISTORY.stream().mapToInt(s -> s.healingDone).sum();
        int mh = HISTORY.stream().mapToInt(s -> s.maxSingleHit).max().orElse(0);
        int tt = HISTORY.stream().mapToInt(s -> s.turns).sum();

        VBox sum = new VBox(4);
        sum.setPadding(new Insets(12));
        sum.setStyle(GameData.CARD_BG);
        sum.getChildren().addAll(
                lbl("总场数：" + total),
                lbl("胜 / 负：" + wins + " / " + (total - wins)),
                lbl(String.format("胜率：%.1f%%", wr)),
                lbl("累计输出：" + td),
                lbl("累计治疗：" + th),
                lbl("最高单次伤害：" + mh),
                lbl("总回合数：" + tt)
        );

        ListView<String> list = new ListView<>();
        list.setStyle("-fx-control-inner-background: #111; -fx-text-fill: #eee;");
        for (int i = HISTORY.size()-1; i >= 0; i--){
            BattleStats s = HISTORY.get(i);
            list.getItems().add(String.format("%s | %s vs %s | %s | %d回合",
                    s.timestamp, s.playerChar, s.aiChar, s.win ? "胜" : "负", s.turns));
        }
        VBox.setVgrow(list, Priority.ALWAYS);
        root.getChildren().addAll(title, sum, list);

        stage.setScene(new Scene(root, 900, 620));
        stage.show();
    }

    private static Label lbl(String s){
        Label l = new Label(s);
        l.setTextFill(Color.web("#ddd")); l.setFont(Font.font(13));
        return l;
    }
}

// ============================================================
// 天梯
// ============================================================
class RankingSystem {
    private static final String FILE = "ranking.dat";
    private static int score = 1000, wins = 0, losses = 0, streak = 0, bestStreak = 0;
    private static String tier = "青铜";

    public static void load(){
        File f = new File(FILE);
        if (!f.exists()){ updateTier(); return; }
        try (DataInputStream dis = new DataInputStream(new FileInputStream(f))){
            score = dis.readInt(); wins = dis.readInt(); losses = dis.readInt();
            streak = dis.readInt(); bestStreak = dis.readInt();
        } catch (Exception ignored) {}
        updateTier();
    }
    public static void save(){
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(FILE))){
            dos.writeInt(score); dos.writeInt(wins); dos.writeInt(losses);
            dos.writeInt(streak); dos.writeInt(bestStreak);
        } catch (Exception ignored) {}
    }
    public static void recordWin(){
        wins++; streak++;
        bestStreak = Math.max(bestStreak, streak);
        score += 10 + Math.min(streak * 2, 20);
        updateTier(); save();
    }
    public static void recordLoss(){
        losses++; streak = 0;
        score = Math.max(0, score - 8);
        updateTier(); save();
    }
    private static void updateTier(){
        if (score < 900) tier = "青铜";
        else if (score < 1100) tier = "白银";
        else if (score < 1300) tier = "黄金";
        else if (score < 1500) tier = "铂金";
        else if (score < 1800) tier = "钻石";
        else if (score < 2100) tier = "大师";
        else tier = "王者";
    }
    public static int getScore(){ return score; }
    public static String getTier(){ return tier; }
    public static int getWins(){ return wins; }
    public static int getLosses(){ return losses; }
    public static int getStreak(){ return streak; }
    public static int getBestStreak(){ return bestStreak; }
    public static String tierIcon(){
        return switch (tier){
            case "青铜" -> "🥉"; case "白银" -> "🥈"; case "黄金" -> "🥇";
            case "铂金" -> "💎"; case "钻石" -> "💠"; case "大师" -> "👑";
            case "王者" -> "🏆"; default -> "🎮";
        };
    }
}