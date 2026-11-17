package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.OrganizedTrolley;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.catalogue.Trolley;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// This class includes TODO comments in addToTrolley() and checkout()
// indicating possible extension ideas to explore.
public class CustomerModel {
    public CustomerView cusView; //part of MVC
    public RemoveProductNotifier removeProductNotifier; // Helper window for stock shortages
    private DatabaseRW databaseRW; //Interface type, not specific implementation
                                  //Benefits: Flexibility: Easily change the database implementation.

    private Product searchResult =null; // product found from search
    private Trolley trolley = createTrolley();
    protected Trolley createTrolley() {
        return new OrganizedTrolley();
    }

    // Data passed to CustomerView for UI updates (including search, trolley and receipt page)
    private String searchResultImageName = "imageHolder.jpg";
                    // Image file name representing the current search result.
                    // - Product image if a product is found
                   // - Default placeholder image otherwise
    private String searchResultProductText = "No product has been searched yet";
                  // Textual representation of the product from the search result.
                  // - Shows product details (e.g. "0001, 40-inch TV, ...") if found
                 // - Otherwise shows a status message
    private String trolleyContentsText = "";
                 // Textual representation of current trolley contents,
                 // used for display in the Trolley page
    private String receiptContentsText = "";
                 // Textual representation of the generated receipt after checkout,
                // used for display in the Receipt page

    // Dependency Injection via constructor
    public CustomerModel(DatabaseRW databaseRW){
        this.databaseRW = databaseRW;
    }

    /**
     * UPDATED search function to support new ID and keyword searching!!
     */
    void search() {
        // reads what user types
        String idText = (cusView.tfId != null) ? cusView.tfId.getText() : "";
        String keywordText = (cusView.tfKeyword != null) ? cusView.tfKeyword.getText() : "";
        // calls search validator to look for a logical match (if there is one)
        searchValidator.validate(idText, keywordText);
        ProductSearchResult outcome;
        // using cases to tell program what to do for each outcome
        switch (searchValidator.getMode()) {
            case BY_ID -> {
                Product p = databaseRW.searchProductById(searchValidator.getQuery());
                outcome = evaluateProduct(p, searchValidator.getQuery());
            }
            case BY_KEYWORD -> {
                // If multiple products match, pick the first available or display first match
                ArrayList<Product> matches = databaseRW.searchProductByKeyword(searchValidator.getQuery());
                if (matches != null && !matches.isEmpty()) {
                    outcome = evaluateProduct(matches.get(0), searchValidator.getQuery());}  else {
                    outcome = new ProductSearchResult(null, SearchStatus.NOT_FOUND,
                            "no product found matching description: " + searchValidator.getQuery());
                }
            }
            case EMPTY -> {
                outcome = new ProductSearchResult(null, SearchStatus.EMPTY_INPUT,
                        SearchStatus.EMPTY_INPUT.getDefaultMessage()); }
            default -> throw new IllegalStateException("unexpected error: " + searchValidator.getMode());
        }
        // shows user result that it found
        this.searchResult = outcome.product();
        this.searchResultProductText = outcome.displayMessage();
        updateView();
    }
    private ProductSearchResult evaluateProduct(Product p, String input) {
        if (p == null) {
            return new ProductSearchResult(null, SearchStatus.NOT_FOUND,
                    "no product found for input: " + input);   }
        // below i used placeholder values within the string in place of calling the values inside just to make writing
        // it all out a little easier (swapping in and out of speech marks would have gotten really messy)
        if (p.getStockQuantity() <= 0) {
            String msg = String.format("Product_Id: %s\n%s\nPrice: £%.2f (sorry, we are out of stock :c )",
                    p.getProductId(), p.getProductDescription(), p.getUnitPrice());
            return new ProductSearchResult(p, SearchStatus.OUT_OF_STOCK, msg);}
        String baseInfo = String.format("Product_Id: %s\n%s,\nprice: £%.2f",
                p.getProductId(), p.getProductDescription(), p.getUnitPrice());
        String quantityInfo = p.getStockQuantity() < 100
                ? String.format("\n%d left in stock.", p.getStockQuantity())
                : "";
        return new ProductSearchResult(p, SearchStatus.SUCCESS, baseInfo + quantityInfo);
    }

    void addToTrolley(){
        if(searchResult != null){
            // now calls OrganizedTrolley.addProduct !
            trolley.addProduct(searchResult);
            trolleyContentsText = ProductListFormatter.buildString(trolley.getProducts());
        }
        else{
            searchResultProductText = "Please search and get an available product before adding to trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        receiptContentsText ="";
        updateView();
    }

    public void checkOut()  {
        if(!trolley.getProducts().isEmpty()){
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: If the trolley is already organized (merged and sorted), grouping is unnecessary.
            ArrayList<Product> groupedProducts= groupProductsById(trolley.getProducts());
            ArrayList<Product> insufficientProducts= databaseRW.purchaseStocks(groupedProducts);

            if(insufficientProducts.isEmpty()){ // If stock is sufficient for all products
                // closes notif windows when stock no longer empty
                if (removeProductNotifier != null) {
                    removeProductNotifier.closeNotifierWindow();
                }
                //get OrderHub and tell it to make a new Order
                OrderHub orderHub =OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(trolley.getProducts());
                trolley.clear();
                trolleyContentsText ="";
                searchResult = null;
                searchResultProductText ="Thank you for shopping with us.";
                receiptContentsText = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );
                System.out.println(receiptContentsText);
            }
            else{ // Some products have insufficient stock — build an error message to inform the customer
                StringBuilder errorMsg = new StringBuilder();
                for(Product p : insufficientProducts){
                    errorMsg.append("\u2022 "+ p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
                searchResult =null;

                //TODO
                // Add the following logic here:
                // 1. Remove products with insufficient stock from the trolley.
                // 2. Trigger a message window to notify the customer about the insufficient stock, rather than directly changing displayLaSearchResult.
                //You can use the provided RemoveProductNotifier class and its showRemovalMsg method for this purpose.
                //remember close the message window where appropriate (using method closeNotifierWindow() of RemoveProductNotifier class)

                // removes product that has run out of stock
                trolley.removeProducts(insufficientProducts);
                // refreshes item list without removed items
                trolleyContentsText = ProductListFormatter.buildString(trolley.getProducts());
                // starts removeProductNotifier
                if (removeProductNotifier != null) {
                    removeProductNotifier.showRemovalMsg(errorMsg.toString());
                }
                searchResultProductText = "Some items were removed due to insufficient stock.";
                System.out.println("stock is not enough... items removed and notification shown");
            }
        }
        else{
            trolleyContentsText = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id,new Product(p.getProductId(),p.getProductDescription(),
                        p.getProductImageName(),p.getUnitPrice(),p.getOrderedQuantity(),p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    /**
     * enum use from week 3 lecture material
     */
    public enum SearchMode {
        BY_ID,
        BY_KEYWORD,
        EMPTY
    }
    /**
     * inner class used to keep input validation in a bubble
     * similar to warehousemodel inner class
     */
    public class SearchInputValidator {
        private String rawInput = "";
        private SearchMode mode = SearchMode.EMPTY;
        public void validate(String idInput, String keywordInput) {
            String trimmedId = (idInput != null) ? idInput.trim() : "";
            String trimmedKeyword = (keywordInput != null) ? keywordInput.trim() : "";
            if (!trimmedId.isEmpty()) {
                rawInput = trimmedId;
                // if input is integer it looks for ID, if anything else it tries to look for keyword
                mode = trimmedId.matches("\\d+") ? SearchMode.BY_ID : SearchMode.BY_KEYWORD;
            } else if (!trimmedKeyword.isEmpty()) {
                rawInput = trimmedKeyword;
                mode = SearchMode.BY_KEYWORD;
            } else {
                rawInput = "";
                mode = SearchMode.EMPTY;
            }
        }
        public String getQuery() { return rawInput; }
        public SearchMode getMode() { return mode; }
    }
    private final SearchInputValidator searchValidator = new SearchInputValidator();

    void cancel(){
        //closes product out of stock notification
        if (removeProductNotifier != null) {
            removeProductNotifier.closeNotifierWindow();
        }
        trolley.clear();
        trolleyContentsText ="";
        updateView();
    }
    void closeReceipt(){
        receiptContentsText ="";
    }

    void updateView() {
        if(searchResult != null){
            searchResultImageName = searchResult.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder + searchResultImageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            searchResultImageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            searchResultImageName = "imageHolder.jpg";
        }
        cusView.update(searchResultImageName, searchResultProductText, trolleyContentsText, receiptContentsText);
    }
     // extra notes:
     //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
     //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    //for test
    public Trolley getTrolley() {
        return trolley;
    }
}
