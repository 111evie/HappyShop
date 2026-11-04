package ci553.happyshop.catalogue;

import java.util.ArrayList;

public class Trolley {

    private ArrayList<Product> products = new ArrayList<>();

    /**
     * Adds a product to the trolley.
     * Currently the product is simply appended to the end of the list.

     * Approach 3 (more advanced):
     * Create a subclass such as OrganizedTrolley that overrides addProduct().
     * Then in CustomerModel:
     *     private Trolley trolley = new OrganizedTrolley();
     * This allows the trolley behaviour to be changed without modifying
     * the existing Trolley class.
     */
    public void addProduct(Product product) {
        products.add(product); // product is appended to the end of the trolley
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public void clear() {
        products.clear();
    }
    /**
     * logic for removing product from trolley by product id
     */
    public void removeProduct(String productId) {
        if (productId != null) {
            products.removeIf(p -> productId.equals(p.getProductId()));
        }
    }
    /**
     * creates array for removing multiple products at once
     */
    public void removeProducts(ArrayList<Product> toRemove) {
        if (toRemove != null) {
            for (Product p : toRemove) {
                removeProduct(p.getProductId());
            }
        }
    }
}
