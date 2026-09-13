import java.util.ArrayList;
import java.util.List;

public class Player {
    private final String name;
    private final int maxHp;
    private final List<Card> hand = new ArrayList<>();
    private int hp;

    public Player(String name, int hp) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
    }

    public void drawCard(Card card) { hand.add(card); }
    public boolean isAlive()        { return hp > 0; }
    public void takeDamage(int dmg) { hp -= dmg; }
    public void heal(int amount)    { hp = Math.min(maxHp, hp + amount); }

    public String getName()     { return name; }
    public int getHp()          { return hp; }
    public int getMaxHp()       { return maxHp; }
    public List<Card> getHand() { return hand; }
}