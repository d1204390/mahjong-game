package example.model;

public class Tile {
    private final TileType type;
    private final int number;

    public Tile(TileType type, int number) {
        this.type = type;
        this.number = number;
    }

    public enum TileType {
        WAN("萬"),   // 萬
        TONG("筒"),  // 筒
        TIAO("條"),  // 條
        WIND("風"),  // 風牌
        DRAGON("字"), // 三元牌
        FLOWER("花"); // 花牌

        private final String chinese;
        TileType(String chinese) {
            this.chinese = chinese;
        }

        public String getChinese() {
            return chinese;
        }
    }

    public TileType getType() {
        return type;
    }

    public int getNumber() {
        return number;
    }

    @Override
    public String toString() {
        if (type == TileType.WIND) {
            return switch (number) {
                case 1 -> "東";
                case 2 -> "南";
                case 3 -> "西";
                case 4 -> "北";
                default -> throw new IllegalStateException("Invalid wind number: " + number);
            };
        } else if (type == TileType.DRAGON) {
            return switch (number) {
                case 1 -> "中";
                case 2 -> "發";
                case 3 -> "白";
                default -> throw new IllegalStateException("Invalid dragon number: " + number);
            };
        } else if (type == TileType.FLOWER) {
            return switch (number) {
                case 1 -> "春";
                case 2 -> "夏";
                case 3 -> "秋";
                case 4 -> "冬";
                case 5 -> "梅";
                case 6 -> "蘭";
                case 7 -> "菊";
                case 8 -> "竹";
                default -> throw new IllegalStateException("Invalid flower number: " + number);
            };
        }
        return number + type.chinese;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tile tile = (Tile) o;
        return number == tile.number && type == tile.type;
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + number;
    }
}