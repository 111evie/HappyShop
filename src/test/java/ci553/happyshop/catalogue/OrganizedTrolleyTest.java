package ci553.happyshop.catalogue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OrganizedTrolleyTest {
    private OrganizedTrolley trolley;
    private Product p1; // id 0001
    private Product p2; // id 0002
    private Product p3; // id 0003

    @BeforeEach
    void setUp() {
        trolley = new OrganizedTrolley();
        p1 = new Product("0001", "40-inch TV", "0001.jpg", 299.99, 1, 10);
        p2 = new Product("0002", "DAB Radio", "0002.jpg", 49.50, 1, 20);
        p3 = new Product("0003", "Toaster", "0003.jpg", 19.99, 1, 15);
    }

    @Test
    void testMergeDuplicateProducts() {
        trolley.addProduct(p1);
        assertEquals(1, trolley.getProducts().size());
        assertEquals(1, trolley.getProducts().get(0).getOrderedQuantity());
        // Add the same product again
        trolley.addProduct(p1);
        assertEquals(1, trolley.getProducts().size(), "should still be 1 line in list");
        assertEquals(2, trolley.getProducts().get(0).getOrderedQuantity(), "item quantity should become 2");
        // Add once more with quantity 3
        Product p1Batch = new Product("0001", "40-inch TV", "0001.jpg", 299.99, 3, 10);
        trolley.addProduct(p1Batch);
        assertEquals(1, trolley.getProducts().size());
        assertEquals(5, trolley.getProducts().get(0).getOrderedQuantity(), "should make item quantity 5");
    }

    @Test
    void testSortingByProductId() {
        // Add in reverse order: 0003, then 0002, then 0001
        trolley.addProduct(p3);
        trolley.addProduct(p2);
        trolley.addProduct(p1);
        assertEquals(3, trolley.getProducts().size());
        assertEquals("0001", trolley.getProducts().get(0).getProductId(), "first id should be 0001");
        assertEquals("0002", trolley.getProducts().get(1).getProductId(), "second id 0002");
        assertEquals("0003", trolley.getProducts().get(2).getProductId(), "third 0003");
    }
}
