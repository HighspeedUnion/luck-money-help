public record Card(String name, String cardType, int value, String description) {

    @Override
    public String toString() {
        return name + "(" + cardType + ", value=" + value + ")";
    }
}