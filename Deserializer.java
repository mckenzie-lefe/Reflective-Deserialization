import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class Deserializer {

    private Map<Integer, Object> objectMap;

    public Deserializer() {
        objectMap = new HashMap<>();
    }

    public Object deserialize(Document document) {
        
        Element rootElement = document.getRootElement();
        List<Element> objectElements = rootElement.getChildren("object");

        //TO DO: handle arrays

        // First pass Create objects and store them in the map
        for (Element objectElement : objectElements) {
            int objectId = Integer.parseInt(objectElement.getAttributeValue("id"));
            String className = objectElement.getAttributeValue("class");

            try {
                Class<?> objClass = Class.forName(className);
                Object obj = objClass.newInstance();
                objectMap.put(objectId, obj);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // Second pass: Populate fields
        for (Element objectElement : objectElements) {
            int objectId = Integer.parseInt(objectElement.getAttributeValue("id"));
            Object obj = objectMap.get(objectId);
            
            List<Element> fieldElements = objectElement.getChildren("field");
            for (Element fieldElement : fieldElements) {
                String fieldName = fieldElement.getAttributeValue("name");
                String declaringClassName = fieldElement.getAttributeValue("declaringclass");

                try {
                    Class<?> declaringClass = Class.forName(declaringClassName);
                    Field field = declaringClass.getDeclaredField(fieldName);
                    field.setAccessible(true);

                    Element valueElement = fieldElement.getChild("value");
                    Element referenceElement = fieldElement.getChild("reference");

                    if (valueElement != null) {
                        // If the field is a primitive, set its value
                        String value = valueElement.getText();
                        setPrimitiveField(field, obj, value);
                    } else if (referenceElement != null) {
                        // If the field is a reference, set the referenced object
                        int referencedId = Integer.parseInt(referenceElement.getText());
                        Object referencedObj = objectMap.get(referencedId);
                        field.set(obj, referencedObj);
                    }
                } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        
        // The root object is the first one in the map
        return objectMap.get(0);

    }
    
    private void setPrimitiveField(Field field, Object obj, String value) throws IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType == int.class) {
            field.setInt(obj, Integer.parseInt(value));
        } else if (fieldType == short.class) {
            field.setShort(obj, Short.parseShort(value));
        } else if (fieldType == long.class) {
            field.setLong(obj, Long.parseLong(value));
        } else if (fieldType == float.class) {
            field.setFloat(obj, Float.parseFloat(value));
        } else if (fieldType == double.class) {
            field.setDouble(obj, Double.parseDouble(value));
        } else if (fieldType == boolean.class) {
            field.setBoolean(obj, Boolean.parseBoolean(value));
        } else if (fieldType == char.class) {
            field.setChar(obj, value.charAt(0));
        }
    }

    public static void main(String[] args) {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
                "<serialized>\r\n" + //
                "  <object class=\"ClassB\" id=\"0\">\r\n" + //
                "    <field name=\"val\" declaringclass=\"ClassB\">\r\n" + //
                "      <reference>1</reference>\r\n" + //
                "    </field>\r\n" + //
                "    <field name=\"val2\" declaringclass=\"ClassB\">\r\n" + //
                "      <reference>2</reference>\r\n" + //
                "    </field>\r\n" + //
                "    <field name=\"val3\" declaringclass=\"ClassB\" />\r\n" + //
                "  </object>\r\n" + //
                "  <object class=\"ClassA\" id=\"1\">\r\n" + //
                "    <field name=\"val\" declaringclass=\"ClassA\">\r\n" + //
                "      <value>3</value>\r\n" + //
                "    </field>\r\n" + //
                "    <field name=\"val2\" declaringclass=\"ClassA\">\r\n" + //
                "      <value>0.2</value>\r\n" + //
                "    </field>\r\n" + //
                "    <field name=\"val3\" declaringclass=\"ClassA\">\r\n" + //
                "      <value>true</value>\r\n" + //
                "    </field>\r\n" + //
                "  </object>\r\n" + //
                "  <object class=\"ClassA\" id=\"2\">\r\n" + //
                "    <field name=\"val\" declaringclass=\"ClassA\">\r\n" + //
                "      <value>12</value>\r\n" + //
                "    </field>\r\n" + //
                "    <field name=\"val2\" declaringclass=\"ClassA\">\r\n" + //
                "      <value>0.2</value>\r\n" + //
                "    </field>\r\n" + //
                "    <field name=\"val3\" declaringclass=\"ClassA\">\r\n" + //
                "      <value>true</value>\r\n" + //
                "    </field>\r\n" + //
                "  </object>\r\n" + //
                "</serialized>";// Provide your XML string here;
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            Document document = saxBuilder.build(new StringReader(xmlString));

            Deserializer deserializer = new Deserializer();
            Object reconstitutedObject = deserializer.deserialize(document);

            // Use the reconstitutedObject as needed
            System.out.println(reconstitutedObject);
            SwingUtilities.invokeLater(() -> {new ObjectVisualizer(reconstitutedObject).setVisible(true);});
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
