package tools.jackson.core.unittest.constraints;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;

import tools.jackson.core.*;
import tools.jackson.core.exc.StreamConstraintsException;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.unittest.JacksonCoreTestBase;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Nesting Depth Constraint Bypass in UTF8DataInputJsonParser
 */
public class DeeplyNestedContentViaDataInputTest extends JacksonCoreTestBase
{
    private static final int TEST_NESTING_DEPTH = 5000;

    private final JsonFactory factory = new JsonFactory();

    // [core#1553] Regression; works in 2.x
    public void testDataInputParserBypassesNestingDepth() throws Exception {
        byte[] data = buildNestedArrays(TEST_NESTING_DEPTH);
        DataInput di = new DataInputStream(new ByteArrayInputStream(data));
        int maxDepth = 0;
        try (JsonParser p = factory.createParser(ObjectReadContext.empty(), di)) {
            while (p.nextToken() != null) {
                int depth = p.currentContext().getNestingDepth();
                if (depth > maxDepth) {
                    maxDepth = depth;
                }
            }
            fail("Should not pass");
        } catch (StreamConstraintsException e) {
            // Expected
            assertTrue(e.getMessage().contains("Document nesting depth"));
        }
        assertTrue(maxDepth < TEST_NESTING_DEPTH,
            "Should have failed before reaching depth " + TEST_NESTING_DEPTH + ", got: " + maxDepth);
    }

    private byte[] buildNestedArrays(int depth) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append('[');
        }
        sb.append("0");
        for (int i = 0; i < depth; i++) {
            sb.append(']');
        }
        return sb.toString().getBytes();
    }
}
