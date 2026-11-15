package com.partyplaylist.model.enums;

public enum PartyPhase {
    WARMUP("Разминка", 2, 3),
    PEAK("Пик", 4, 5),
    SLOW("Медляк", 1, 2);

    private final String title;
    private final int minEnergy;
    private final int maxEnergy;

    PartyPhase(String title, int minEnergy, int maxEnergy) {
        this.title = title;
        this.minEnergy = minEnergy;
        this.maxEnergy = maxEnergy;
    }

    public String getTitle() {
        return title;
    }

    public int getMinEnergy() {
        return minEnergy;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    public String getKey() {
        return name().toLowerCase();
    }

    public boolean accepts(int energy) {
        return energy >= minEnergy && energy <= maxEnergy;
    }
}
