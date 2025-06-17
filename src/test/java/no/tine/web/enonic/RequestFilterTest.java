package no.tine.web.enonic;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic tests for RequestFilter JSON body processing functionality
 */
public class RequestFilterTest {

    @Test
    public void testJsonProcessing() {
        RequestFilter filter = new RequestFilter();
        
        try {
            // Test JSON object processing
            String jsonInput = "{\"username\":\"test\",\"password\":\"secret123\"}";
            String result = filter.processJsonBody(jsonInput);
            
            JSONObject resultObject = new JSONObject(result);
            assertEquals("test", resultObject.getString("username"));
            assertEquals("xxxxx", resultObject.getString("password"));
            
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test 
    public void testJsonArrayProcessing() {
        RequestFilter filter = new RequestFilter();
        
        try {
            // Test JSON array processing
            String jsonInput = "[{\"username\":\"test1\",\"password\":\"secret1\"},{\"username\":\"test2\"}]";
            String result = filter.processJsonBody(jsonInput);
            
            assertTrue(result.contains("\"password\":\"xxxxx\""));
            assertTrue(result.contains("\"username\":\"test1\""));
            assertTrue(result.contains("\"username\":\"test2\""));
            
        } catch (Exception e) {
            fail("Should not throw exception: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidJson() {
        RequestFilter filter = new RequestFilter();
        
        try {
            // Test invalid JSON handling
            String invalidJson = "not valid json";
            String result = filter.processJsonBody(invalidJson);
            
            assertEquals(invalidJson, result);
            
        } catch (Exception e) {
            fail("Should not throw exception for invalid JSON: " + e.getMessage());
        }
    }
}