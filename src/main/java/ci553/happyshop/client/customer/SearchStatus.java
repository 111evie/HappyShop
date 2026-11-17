package ci553.happyshop.client.customer;
/**
 * using enums to make a more user friendly product search interface
 */
public enum SearchStatus {
    SUCCESS("Product found :)"),
    NOT_FOUND("No product matched your search query... :("),
    OUT_OF_STOCK("Product found, but out of stock :( "),
    EMPTY_INPUT("Please enter a 4-digit Product ID or a Keyword!!");
    private final String defaultMessage;
    SearchStatus(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }
    public String getDefaultMessage() {
        return defaultMessage;
    }
}