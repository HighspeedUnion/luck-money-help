# 🔥 火柴杀（Matchstick Kill）

> 回合制卡牌对战游戏 · JavaFX 桌面版
>
> **版本：** `UI 26.2 · hk 26.2` · **构建：** 单文件 Java

---

## 📖 简介

火柴杀是一款极简却深度十足的回合制卡牌对战游戏。**28 位风格迥异的角色**、**31 件各有妙用的装备**、**27 种功能卡牌**，组合出千变万化的对局。支持单人 vs AI 与本地双人对战，附带图鉴、统计、天梯等完整辅助系统。

---

## ✨ 特性

| 类别 | 内容 |
|------|------|
| 🎭 **28 位角色** | 从坦克到刺客，从法师到辅助，各有 2~3 个独特技能 |
| ⚙️ **31 件装备** | 通用 + 专属，装备在对应角色身上时触发额外效果 |
| 🃏 **27 种卡牌** | 攻击 / 治疗 / 闪避 / 护盾 / 增益 / 减益 / 特殊 |
| 🤖 **AI 对手** | 启发式决策：血低治疗、残血收割、优先装备 |
| 📖 **图鉴系统** | 所有角色/装备/卡牌可查询，支持模糊搜索 |
| 📊 **对战统计** | 历史战绩 + 胜率 + 累计输出/治疗 + 最高单次伤害 |
| 🏅 **天梯积分** | 7 段位（青铜 → 王者）+ 连胜加成 |
| 🎨 **暗色主题** | 渐变背景 + 彩色日志 + 卡片按类型配色 |
| ⌨️ **快捷键** | `空格` 结束回合，`S` 打开技能菜单 |

---

## 🚀 快速开始

### 环境要求

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | **25** 或更高 | 推荐 [BellSoft Liberica Full](https://bell-sw.com/pages/downloads/)，内置 JavaFX |
| JavaFX | **25** | Liberica Full 版已内置；普通 JDK 需单独下载 SDK |
| 操作系统 | Windows / Linux / macOS | 均可 |

### 方式一：从源码编译（推荐）

```bash
# 1. 编译
javac -encoding UTF-8 -d out GameApp.java

# 2. 打包为 JAR
jar --create --file GameApp.jar --main-class GameApp -C out .

# 3. 运行
java --enable-native-access=javafx.graphics -jar GameApp.jar
```

### 方式二：单文件直接运行（Java 11+）

```bash
java GameApp.java
```

### 方式三：打包成独立可执行文件（jpackage）

```bash
# 生成精简运行时
jlink --add-modules java.base,javafx.controls,javafx.graphics,javafx.base,javafx.media \
      --output runtime --strip-debug --no-header-files --no-man-pages --compress=2

# 打成绿色文件夹
jpackage --type app-image \
         --name "火柴杀" \
         --input . \
         --main-jar GameApp.jar \
         --main-class GameApp \
         --runtime-image runtime \
         --app-version 26.2

# 输出在 火柴杀/ 文件夹，双击 bin/火柴杀 即可运行
```

---

## 🎮 玩法

### 回合流程

```
摸牌 → 出牌（可多张） → 用技能 → 结束回合 → 对手回合
```

### 核心规则

| 规则 | 说明 |
|------|------|
| 初始手牌 | 双方各摸 3 张 |
| 每回合摸牌 | 2 张（手牌为空摸 3 张） |
| 攻击上限 | 每回合最多 2 张攻击牌（装备加特林 +2，枪手配双枪 +1） |
| 手牌上限 | 20 张（被腐毒侵蚀等可永久降低） |
| 伤害结算 | 先扣护盾，再扣 HP |
| 死亡判定 | HP ≤ 0 且无复活技能则落败 |

### 卡牌类型

| 类型 | 图标 | 说明 |
|------|------|------|
| 攻击 | ⚔ | 造成伤害 |
| 物理 | 🗡 | 造成等同物攻的伤害 |
| 法术 | 🔮 | 造成等同法攻的伤害 |
| 治疗 | 💚 | 恢复 HP |
| 闪避 | 💨 | 规避单次伤害 |
| 护盾 | 🛡 | 抵挡伤害，优先扣除 |
| 增益 | 🔥 | 狂暴、沉思、强化 |
| 减益 | ☠ | 中毒、灼烧、虚弱 |
| 装备 | ⚙ | 装备到对应槽位 |
| 特殊 | ⚡ | 决斗、末日、献祭 |

### 胜负条件

对手 HP ≤ 0 且无复活技能时获胜。部分角色有复活能力：

- **秦默** — 献祭手牌复活（最多 2 次，第 1 次 5 张，第 2 次 10 张）
- **多斯** — 自动复活（每局 1 次）

---

## 🎭 角色一览

### 原版 16 位

| 角色 | HP | 物攻 | 法攻 | 定位 |
|------|:--:|:---:|:---:|------|
| 拾荒者 | 6 | 1 | 1 | 高坦度装备拉扯坦克 |
| 毒猎 | 5 | 2 | 1 | 持续磨血 + 资源压制 |
| 罗伊 | 5 | 1 | 2 | 法伤抗压 + 自带反伤 |
| 吕山 | 3 | 2 | 1 | 手牌爆发 + 单控收割 |
| 钟离 | 2 | 2 | 2 | 高频小技能 + 全场 AOE |
| 利刃 | 3 | 2 | 1 | 后期成长型输出 |
| 枪手 | 4 | 2 | 1 | 冷却压制 + 回合强控 |
| 帝郡 | 4 | 1 | 2 | 持续减益 + 伤害转移 |
| 冷锋 | 3 | 1 | 2 | 防御兜底 + 高额法伤 |
| 秦默 | 2 | 2 | 2 | 手牌博弈 + 有限复活 |
| 玖恒 | 4 | 1 | 1 | 装备拓展 + 强控 |
| 多斯 | 3 | 1 | 1 | 全队续航核心 |
| 虚无 | 4 | 1 | 3 | 高法伤 + 全场削弱 |
| 铁骑 | 6 | 2 | 1 | 高血肉盾 + 嘲讽 |
| 游侠 | 3 | 3 | 1 | 高速单体爆发 |
| 圣女 | 4 | 1 | 2 | 团队辅助 + 强力治疗 |

### 追加 12 位

| 角色 | HP | 物攻 | 法攻 | 定位 |
|------|:--:|:---:|:---:|------|
| 影武者 | 4 | 2 | 1 | 高闪避 + 反击刺客 |
| 圣殿骑士 | 7 | 1 | 2 | 高血高护盾辅助坦克 |
| 元素使 | 3 | 1 | 3 | 四元素切换型法师 |
| 赏金猎人 | 3 | 3 | 0 | 猎杀收益型输出 |
| 龙骑 | 5 | 2 | 2 | 均衡型战士 |
| 医者 | 3 | 1 | 2 | 治疗 + 净化辅助 |
| 狂战士 | 5 | 3 | 0 | 低血高伤纯物理 |
| 符卡师 | 4 | 1 | 3 | 符卡多面手 |
| 巫医 | 4 | 1 | 2 | 中毒 + 减速控制 |
| 决斗者 | 4 | 2 | 2 | 单挑特化 |
| 占星师 | 3 | 1 | 3 | 预言型法师 |
| 武僧 | 5 | 2 | 1 | 连击型近战 |

---

## 📂 项目结构

```
matchstick/
├── GameApp.java        # 全部代码（单文件，含所有类）
├── GameApp.jar         # 编译产物
├── ranking.dat         # 天梯积分存档（自动生成）
├── battle_stats.dat    # 对战统计存档（自动生成）
└── README.md
```

编译后生成的 class 文件：

```
GameApp.class
GameData.class
GameData$Char.class / $Skill.class / $Equip.class
Card.class
Equipment.class
Player.class
BattleEngine.class
AIPlayer.class
BattleUI.class
CardPool.class
CharDex.class
BattleStats.class
RankingSystem.class
```

---

## ⌨️ 快捷键

| 按键 | 功能 |
|------|------|
| `空格` | 结束回合 |
| `S` | 打开技能菜单 |
| 鼠标左键 | 点击手牌出牌 |

---

## 🖼️ 界面预览

```
┌─────────────────────────────────────────────────────────┐
│  第 3 回合 · 玩家1     火柴杀 · UI 26.2 · hk 26.2       │
├─────────────────────────────────────────────────────────┤
│ ┌──────────────────────┐  ┌──────────────────────┐      │
│ │ 玩家1 · 影武者 ◀     │  │ 玩家2 · 元素使       │      │
│ │ ❤ 4/4                │  │ ❤ 3/3                │      │
│ │ ████████████████████ │  │ ████████████████░░░░ │      │
│ │ ⚔2 🔮1 🎴5/20        │  │ ⚔1 🔮3 🎴4/20        │      │
│ │ [影之刃] [飞行滑板]   │  │ [元素之心]            │      │
│ │ 💨×1 🛡2              │  │ 🔥2                  │      │
│ └──────────────────────┘  └──────────────────────┘      │
├─────────────────────────────────────────────────────────┤
│ --- 第 3 回合 · 玩家1 ---                                │
│ 玩家1 摸了 2 张牌                                        │
│ 🌑 影袭 2 点 + 1闪避                                     │
│ 玩家1 对 玩家2 造成 3 点物理伤害                          │
├─────────────────────────────────────────────────────────┤
│ [普攻·直击] [普攻·重击] [躲闪] [疗伤] [影之刃]           │
│ [技能] [结束回合]                                        │
└─────────────────────────────────────────────────────────┘
```

---

## 🛠️ 技术栈

| 组件 | 版本 |
|------|------|
| Java | 25 (LTS) |
| JavaFX | 25 |
| 渲染管线 | Prism → ES2 / D3D9 / SW |
| 构建工具 | `javac` + `jar` + `jpackage` |
| 存档格式 | Java 原生序列化（`.dat`） |

---

## 📝 更新日志

### v26.2 (最新)

**新增**
- 📖 图鉴系统（角色/装备/卡牌 + 模糊搜索）
- 📊 对战统计（历史记录 + 汇总分析）
- 🏅 天梯积分（7 段位 + 连胜加成）
- 🎴 卡牌池重构（`CardPool` 独立可复用）

**平衡调整**
- 削弱：圣光庇护、势不可挡、致命偷袭、龙骑 HP、群体治疗
- 加强：赏金猎人、占星师、重甲犀牛、幽灵马车、影之刃、龙鳞甲
- 调整：加特林不再完全解除攻击上限（改为 +2）、双枪 3→2 张

**优化**
- 单文件架构，一次编译生成所有 class
- 移除时间锁，正式版永久可用

### v26.1

- 28 角色 + 31 装备 + 27 卡牌
- AI 对手
- 暗色主题 UI
- 彩色日志系统

---

## 🎯 后续计划

- [ ] 被动技能完整激活（影武者残影、赏金猎人悬赏、狂战士嗜血等）
- [ ] 局域网联机（权威主机模式）
- [ ] 成就系统（30 个）
- [ ] 战场环境（每局随机 8 种之一）
- [ ] 战斗回放
- [ ] 存档 / 读档
- [ ] 音效 + 背景音乐
- [ ] 伤害飘字动画
- [ ] AI 难度分级
- [ ] 每日挑战模式
- [ ] Android 版

---

## ❓ 常见问题

### Q1：运行报错 `Error: JavaFX runtime components are missing`

说明你用的 JDK 未内置 JavaFX。两种解决方法：

1. **换用 Liberica Full JDK**（推荐）—— https://bell-sw.com/pages/downloads/
2. 下载 [OpenJFX SDK](https://openjfx.io/)，运行时加 `--module-path`：

   ```bash
   java --module-path /path/to/javafx-sdk/lib \
        --add-modules javafx.controls \
        -jar GameApp.jar
   ```

### Q2：警告 `restricted method in java.lang.System has been called`

这是 Java 24+ 对本地库加载的新提示，不影响使用。想消除：

```bash
java --enable-native-access=javafx.graphics -jar GameApp.jar
```

### Q3：`java -jar GameApp.jar` 打不开

检查 `META-INF/MANIFEST.MF` 是否包含：

```
Main-Class: GameApp
```

如果没有，重新打包时显式指定：

```bash
jar --create --file GameApp.jar --main-class GameApp -C out .
```

### Q4：Windows 下双击 JAR 没反应

- 可能是 `.jar` 关联到了错误的程序
- 建议用命令行运行看具体错误：`java -jar GameApp.jar`
- 或者右键 JAR → 打开方式 → 选 `javaw.exe`

### Q5：如何查看 JIT 编译情况？

运行时加参数：

```bash
java -XX:+PrintCompilation -jar GameApp.jar
```

游戏启动初期可能略慢，几回合后 JIT 预热完成会明显流畅。

---

## 🤝 贡献

欢迎提交 Issue 和 PR：

- 🐛 发现 bug → 提 Issue 描述复现步骤
- 💡 新角色/装备/卡牌 → 直接提 PR 修改 `GameData`
- 🎨 UI 改进 → 提 PR 说明视觉效果
- ⚖️ 平衡建议 → 提 Issue 附对局数据

---

## 📄 许可证

**GNU General Public License v3.0**

```
Copyright (C) 2026 hk

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
```

完整协议见 [LICENSE](LICENSE) 或 https://www.gnu.org/licenses/gpl-3.0.txt

---

## 🔗 相关链接

- [BellSoft Liberica JDK 下载](https://bell-sw.com/pages/downloads/)
- [OpenJFX 官网](https://openjfx.io/)
- [GNU GPL v3.0 全文](https://www.gnu.org/licenses/gpl-3.0.txt)

---

<div align="center">

**🔥 火柴杀 🔥**

*28 位角色 · 31 件装备 · 27 种卡牌 · 无限组合*

**祝你游戏愉快！**

</div>
