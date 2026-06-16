package tennispredict.data;

public class Player {

    private final int id;
    private final String name;
    private final char hand;
    private final int heightCm;

    public Player(int id, String name, char hand, int heightCm) {
        this.id = id;
        this.name = name;
        this.hand = hand;
        this.heightCm = heightCm;
    }

    public int getId() { return this.id; }
    public String getName() { return this.name; }
    public char getHand() { return this.hand; }
    public int getHeightCm() { return this.heightCm; }

    @Override
    public String toString() {
        return this.name + " (#" + this.id + ")";
    }
}
