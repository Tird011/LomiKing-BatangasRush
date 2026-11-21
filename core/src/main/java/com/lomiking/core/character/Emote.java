package com.lomiking.core.character;

/** Mang Kaloy's face expressions. col/row point at the cell in textures/face_atlas.png (4 x 2 grid). */
public enum Emote {
    HAPPY_MASKED(0, 0), HAPPY(1, 0), BLOW(2, 0), LAUGH(3, 0),
    SHOCKED(0, 1), NERVOUS(1, 1), UH_OH(2, 1);

    private final int atlasCol, atlasRow;
    Emote(int c, int r) { atlasCol = c; atlasRow = r; }
    public int getAtlasCol() { return atlasCol; }
    public int getAtlasRow() { return atlasRow; }
}
