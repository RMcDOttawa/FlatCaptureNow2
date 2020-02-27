public enum BinningAvailability {
    OFF,
    AVAILABLE,
    DEFAULT;

    public int codeNumber() {
        int result = -1;
        switch (this) {
            case OFF:
                result = 0;
                break;
            case AVAILABLE:
                result = 1;
                break;
            case DEFAULT:
                result = 2;
                break;
        }
        return result;
    }
}
