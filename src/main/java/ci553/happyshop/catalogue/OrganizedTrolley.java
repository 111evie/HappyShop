package ci553.happyshop.catalogue;

import java.util.Collections;

public class OrganizedTrolley extends Trolley {

    @Override
    public void addProduct(Product product) {
        // makes sure product id is in warehouse database
        if (product == null) {
            return;
        }
        // Check if a product is already in trolley
        boolean merged = false;
        for (Product existing : getProducts()) {
            if (existing.getProductId().equals(product.getProductId())) {
                // Merge quantities
                existing.setOrderedQuantity(existing.getOrderedQuantity() + product.getOrderedQuantity());
                merged = true;
                break;
            }
        }
        // if product not found a new one is created
        if (!merged) {
            Product copy = new Product(
                    product.getProductId(),
                    product.getProductDescription(),
                    product.getProductImageName(),
                    product.getUnitPrice(),
                    product.getOrderedQuantity(),
                    product.getStockQuantity()
            );
            super.addProduct(copy);
        }
        // Keep products sorted by id for better ordering
        Collections.sort(getProducts());
    }
}

