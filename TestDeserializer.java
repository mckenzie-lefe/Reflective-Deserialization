import org.junit.Test;
import ObjectPool.*;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.List;
import org.jdom2.*;
import java.io.StringReader;

import org.jdom2.input.SAXBuilder;

public class TestDeserializer {

    @Test
    public void testDeserializeSimpleObject() {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<serialized>\r\n" +
                "  <object class=\"ObjectPool.SimpleObject\" id=\"0\">\r\n" +
                "    <field name=\"primitiveInt\" declaringclass=\"ObjectPool.SimpleObject\">\r\n" +
                "      <value>42</value>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "</serialized>";

        Deserializer deserializer = new Deserializer();
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            List<Object> deserializedObjects = (List<Object>) deserializer.deserialize(document);

            assertTrue(deserializedObjects.get(0) instanceof SimpleObject);
            assertEquals(42, ((SimpleObject) deserializedObjects.get(0)).primitiveInt);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during deserialization");
        }
    }

    @Test
    public void testDeserializeArrayOfObjects() {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<serialized>\r\n" +
                "  <object class=\"[LObjectPool.SimpleObject;\" id=\"0\" length=\"3\">\r\n" +
                "    <reference>1</reference>\r\n" +
                "    <reference>1</reference>\r\n" +
                "    <reference>1</reference>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"ObjectPool.SimpleObject\" id=\"1\">\r\n" +
                "    <field name=\"primitiveInt\" declaringclass=\"ObjectPool.SimpleObject\">\r\n" +
                "      <value>42</value>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "</serialized>";

        Deserializer deserializer = new Deserializer();
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            List<Object> deserializedObjects = (List<Object>) deserializer.deserialize(document);

            assertTrue(deserializedObjects.get(0).getClass().isArray());
            Object[] array = (Object[]) deserializedObjects.get(0);
            assertEquals(3, array.length);

            for (Object element : array) {
                assertTrue(element instanceof SimpleObject);
                assertEquals(42, ((SimpleObject) element).primitiveInt);
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during deserialization");
        }
    }

    @Test
    public void testDeserializeCircularReference() {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<serialized>\r\n" +
                "  <object class=\"ObjectPool.CircularReference\" id=\"0\">\r\n" +
                "    <field name=\"circularRef\" declaringclass=\"ObjectPool.CircularReference\">\r\n" +
                "      <reference>1</reference>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"ObjectPool.CircularReference\" id=\"1\">\r\n" +
                "    <field name=\"circularRef\" declaringclass=\"ObjectPool.CircularReference\">\r\n" +
                "      <reference>0</reference>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "</serialized>";

        Deserializer deserializer = new Deserializer();
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            List<Object> deserializedObjects = (List<Object>) deserializer.deserialize(document);

            assertEquals(2, deserializedObjects.size());

            Object firstObject = deserializedObjects.get(0);
            Object secondObject = deserializedObjects.get(1);

            assertTrue(firstObject instanceof CircularReference);
            assertTrue(secondObject instanceof CircularReference);

            CircularReference firstCircularRef = (CircularReference) firstObject;
            CircularReference secondCircularRef = (CircularReference) secondObject;

            assertNotNull(firstCircularRef.circularRef);
            assertNotNull(secondCircularRef.circularRef);

            assertSame(secondCircularRef, firstCircularRef.circularRef);
            assertSame(firstCircularRef, secondCircularRef.circularRef);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during deserialization");
        }
    }

    @Test
    public void testDeserializeCollectionInstance() {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<serialized>\r\n" +
                "  <object class=\"ObjectPool.CollectionInstance\" id=\"0\">\r\n" +
                "    <field name=\"objectRefs\" declaringclass=\"ObjectPool.CollectionInstance\">\r\n" +
                "      <reference>1</reference>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"java.util.ArrayList\" id=\"1\" length=\"3\">\r\n" +
                "    <reference>2</reference>\r\n" +
                "    <reference>3</reference>\r\n" +
                "    <reference>4</reference>\r\n" + 
                "  </object>\r\n" +
                "  <object class=\"ObjectPool.SimpleObject\" id=\"2\">\r\n" +
                "    <field name=\"primitiveInt\" declaringclass=\"ObjectPool.SimpleObject\">\r\n" +
                "      <value>55</value>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"ObjectPool.ReferenceSimpleObject\" id=\"3\">\r\n" +
                "    <field name=\"simpleObj\" declaringclass=\"ObjectPool.ReferenceSimpleObject\">\r\n" +
                "      <reference>2</reference>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"ObjectPool.CircularReference\" id=\"4\">\r\n" +
                "    <field name=\"circularRef\" declaringclass=\"ObjectPool.CircularReference\" />\r\n" +
                "  </object>\r\n" +
                "</serialized>";

        Deserializer deserializer = new Deserializer();
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            List<Object> deserializedObjects = (List<Object>) deserializer.deserialize(document);

            assertEquals(5, deserializedObjects.size());
            Object deserializedObject = deserializedObjects.get(0);

            assertTrue(deserializedObject instanceof CollectionInstance);

            CollectionInstance collectionInstance = (CollectionInstance) deserializedObject;
            assertEquals(3, collectionInstance.objectRefs.size());

            assertEquals(55, ((SimpleObject)collectionInstance.objectRefs.get(0)).primitiveInt);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during deserialization");
        }
    }

    @Test
    public void testDeserializeArrayOfPrimitives() {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<serialized>\r\n" +
                "  <object class=\"ObjectPool.ArrayOfPrimitives\" id=\"0\">\r\n" +
                "    <field name=\"intArr\" declaringclass=\"ObjectPool.ArrayOfPrimitives\">\r\n" +
                "      <reference>1</reference>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"[I\" id=\"1\" length=\"3\">\r\n" +
                "    <value>1</value>\r\n" +
                "    <value>2</value>\r\n" +
                "    <value>3</value>\r\n" +
                "  </object>\r\n" +
                "</serialized>";

        Deserializer deserializer = new Deserializer();
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            List<Object> deserializedObjects = (List<Object>) deserializer.deserialize(document);

            assertEquals(2, deserializedObjects.size());
            Object deserializedObject = deserializedObjects.get(0);

            assertTrue(deserializedObject instanceof ArrayOfPrimitives);

            ArrayOfPrimitives arrayOfPrimitives = (ArrayOfPrimitives) deserializedObject;
            int[] primitiveArray = arrayOfPrimitives.intArr;

            assertEquals(3, primitiveArray.length);
            assertArrayEquals(new int[]{1, 2, 3}, primitiveArray);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during deserialization");
        }
    }

    @Test
    public void testDeserializeArrayOfObjectsWithNulls() {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<serialized>\r\n" +
                "  <object class=\"[LObjectPool.SimpleObject;\" id=\"0\" length=\"3\">\r\n" +
                "    <reference>1</reference>\r\n" +
                "    <value>null</value>\r\n" +
                "    <reference>2</reference>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"ObjectPool.SimpleObject\" id=\"1\">\r\n" +
                "    <field name=\"primitiveInt\" declaringclass=\"ObjectPool.SimpleObject\">\r\n" +
                "      <value>42</value>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "  <object class=\"ObjectPool.SimpleObject\" id=\"2\">\r\n" +
                "    <field name=\"primitiveInt\" declaringclass=\"ObjectPool.SimpleObject\">\r\n" +
                "      <value>56</value>\r\n" +
                "    </field>\r\n" +
                "  </object>\r\n" +
                "</serialized>";

        Deserializer deserializer = new Deserializer();
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document document = saxBuilder.build(new StringReader(xmlString));
            List<Object> deserializedObject = (List<Object>) deserializer.deserialize(document);

            assertTrue(deserializedObject.get(0).getClass().isArray());
            Object[] array = (Object[]) deserializedObject.get(0);
            assertEquals(3, array.length);

            assertTrue(array[0] instanceof SimpleObject);
            assertNull(array[1]); // null element in the array

            assertTrue(array[2] instanceof SimpleObject);
            assertEquals(56, ((SimpleObject) array[2]).primitiveInt);

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during deserialization");
        }
    }

    @Test
    public void testDeserializeEmptyDocument() {
        String emptyXmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
                "<serialized></serialized>";

        Deserializer deserializer = new Deserializer();
        SAXBuilder saxBuilder = new SAXBuilder();

        try {
            Document emptyDocument = saxBuilder.build(new StringReader(emptyXmlString));
            List<Object> deserializedObjects = (List<Object>) deserializer.deserialize(emptyDocument);

            assertTrue(deserializedObjects.isEmpty());

        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception during deserialization");
        }
    }
}
