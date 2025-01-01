package example.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Meld {
    private final MeldType type;
    private final List<Tile> tiles;

    public Meld(MeldType type, List<Tile> tiles) {
        this.type = type;
        this.tiles = new ArrayList<>(tiles);
    }

    public MeldType getType() {
        return type;
    }

    public List<Tile> getTiles() {
        return Collections.unmodifiableList(tiles);
    }

    @Override
    public String toString() {
        return type + ": " + tiles;
    }
}