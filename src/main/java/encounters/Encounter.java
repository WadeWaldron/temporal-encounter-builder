package encounters;

import java.util.List;

public record Encounter(Integer xpThreshold, List<Creature> creatures, String description) {
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("╔════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    ENCOUNTER GENERATED                     ║\n");
        sb.append("╚════════════════════════════════════════════════════════════╝\n\n");
        
        sb.append("📋 ").append(description.lines().findFirst().orElse(description)).append("\n");
        String rest = description.replaceFirst("^[^\n]*\n?", "");
        if (!rest.isBlank()) {
            rest.lines().forEach(line -> sb.append("   ").append(line).append("\n"));
        }
        
        sb.append("\n⚔️  Combat Threats:\n");
        for (int i = 0; i < creatures.size(); i++) {
            Creature c = creatures.get(i);
            sb.append(String.format("  %d. %-20s (CR %.1f) | %,d XP | %s (%s)\n", 
                i + 1, c.name(), c.cr(), c.xp(), c.type(), c.size()));
            sb.append(String.format("     └─ %s\n", c.summary()));
        }
        
        int totalXp = creatures.stream().mapToInt(Creature::xp).sum();
        sb.append("\n💰 Challenge Summary:\n");
        sb.append(String.format("   • Total XP Budget: %,d\n", xpThreshold));
        sb.append(String.format("   • Encounter XP: %,d\n", totalXp));
        sb.append(String.format("   • Creature Count: %d\n", creatures.size()));
        
        return sb.toString();
    }
}
