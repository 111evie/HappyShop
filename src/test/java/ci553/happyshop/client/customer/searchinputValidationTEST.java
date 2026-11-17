package ci553.happyshop.client.customer;
import ci553.happyshop.storageAccess.DatabaseRW;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;



/**
 * TESTING for new sdearch input validation additions
 */


@DisplayName("search input validating and search function tests")
class SearchInputValidatorTest {
    private CustomerModel model;
    private CustomerModel.SearchInputValidator validator;
    @BeforeEach
    void setUp() {
        DatabaseRW mockDb = mock(DatabaseRW.class);
        model = new CustomerModel(mockDb);
        validator = model.new SearchInputValidator();
    }
    @Test
    @DisplayName("empty input should trigger EMPTY case mode")
    void testEmptyInputMode() {
        validator.validate("", "");
        assertEquals(CustomerModel.SearchMode.EMPTY, validator.getMode());
        assertEquals("", validator.getQuery());
    }
    @Test
    @DisplayName("four digit integer input triggers ID SEARCH case mode")
    void testIdInputMode() {
        validator.validate("0003", "");
        assertEquals(CustomerModel.SearchMode.BY_ID, validator.getMode());
        assertEquals("0003", validator.getQuery());
    }
    @Test
    @DisplayName("text input triggers KEYWORD/DESCRIPTION case mode and should attempt to find description with the input")
    void testKeywordInputMode() {
        validator.validate("", "toaster");
        assertEquals(CustomerModel.SearchMode.BY_KEYWORD, validator.getMode());
        assertEquals("toaster", validator.getQuery());
    }
}