package encounters;

import java.util.Random;

public class CreatureBuilder {
    private static final Random rnd = new Random();

    private String name;
    private Double cr;
    private Integer xp;
    private String type;
    private String size;
    private String summary;

    public CreatureBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public CreatureBuilder withCr(double cr) {
        this.cr = cr;
        return this;
    }

    public CreatureBuilder withXp(int xp) {
        this.xp = xp;
        return this;
    }

    public CreatureBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public CreatureBuilder withSize(String size) {
        this.size = size;
        return this;
    }

    public CreatureBuilder withSummary(String summary) {
        this.summary = summary;
        return this;
    }

    public Creature build() {
        return new Creature(
            name != null ? name : "Creature" + rnd.nextInt(1000),
            cr != null ? cr : rnd.nextDouble() * 20,
            xp != null ? xp : rnd.nextInt(100000),
            type != null ? type : randomType(),
            size != null ? size : randomSize(),
            summary != null ? summary : "Description" + rnd.nextInt(1000)
        );
    }

    private static String randomType() {
        String[] types = {"Humanoid", "Beast", "Monstrosity", "Dragon", "Giant", "Fiend", "Undead", "Celestial"};
        return types[rnd.nextInt(types.length)];
    }

    private static String randomSize() {
        String[] sizes = {"Small", "Medium", "Large", "Huge", "Gargantuan"};
        return sizes[rnd.nextInt(sizes.length)];
    }
}
