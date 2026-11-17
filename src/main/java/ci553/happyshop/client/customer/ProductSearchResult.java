package ci553.happyshop.client.customer;
import ci553.happyshop.catalogue.Product;
/**
 * using record class to handle search result outcome (from week 3 lectur)e
 **/
public record ProductSearchResult(
        Product product,
        SearchStatus status,
        String displayMessage
) {
    /**
     validation/checking for error or success
     */
    public ProductSearchResult {
        if (status == null) {
            throw new IllegalArgumentException("SearchStatus cannot be null");
        }
        if (displayMessage == null || displayMessage.isBlank()) {
            displayMessage = status.getDefaultMessage();
        }
    }
    public boolean isAvailable() {
        return status == SearchStatus.SUCCESS && product != null && product.getStockQuantity() > 0;
    }
}