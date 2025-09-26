public class GamePlayer {
    private String name;
    private String character;
    private int maxHp;
    private int hp;
    private int damage;

    public GamePlayer(String name, String character) {
        this.name = name;
        this.character = character;
        if (character.equalsIgnoreCase("caballero")) {
            this.maxHp = 150;
            this.damage = 20;
        } else if (character.equalsIgnoreCase("mago")) {
            this.maxHp = 100;
            this.damage = 10;
        } else if (character.equalsIgnoreCase("vampiro")) {
            this.maxHp = 120;
            this.damage = 15;
        } else {
            this.maxHp = 100;
            this.damage = 10;
        }
        this.hp = this.maxHp;
    }

    public String getName() { return name; }
    public String getCharacter() { return character; }
    public int getHp() { return hp; }
    public int getMaxHp() { return maxHp; }
    public int getDamage() { return damage; }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }

    public void takeDamage(int amount) {
        hp -= amount;
    }

    public boolean isAlive() {
        return hp > 0;
    }
}
