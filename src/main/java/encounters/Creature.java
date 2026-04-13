package encounters;

public record Creature(
    String name,
    double cr,
    int xp,
    String type,
    String size,
    String summary
) {
    public Creature {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Creature name cannot be blank");
        }
        if (cr < 0) {
            throw new IllegalArgumentException("Challenge Rating cannot be negative");
        }
        if (xp < 0) {
            throw new IllegalArgumentException("XP value cannot be negative");
        }
        if (summary == null || summary.isBlank()) {
            throw new IllegalArgumentException("Creature summary cannot be blank");
        }
    }
}
