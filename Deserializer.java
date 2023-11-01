import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.lang.reflect.*;
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

        // First pass Create objects and store them in the map
        for (Element objectElement : objectElements) {
            Object obj = null;
            String className = objectElement.getAttributeValue("class");

            try {
                Class<?> objClass = Class.forName(className);

                if (objClass.isArray())
                    obj = Array.newInstance(objClass.getComponentType(), Integer.valueOf(objectElement.getAttributeValue("length")));

                else {
                    Constructor con = objClass.getDeclaredConstructor();
                    con.setAccessible(true);
                    obj = con.newInstance();
                }

                objectMap.put(Integer.parseInt(objectElement.getAttributeValue("id")), obj);
            } catch (ClassNotFoundException |  InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        // Second pass: Populate fields
        for (Element objectElement : objectElements) {
            

            Object obj = objectMap.get(objectElement.getAttributeValue("id"));

            if (obj == null)
                continue;

            Class<?> clazz = obj.getClass();
            List<Element> elements = objectElement.getChildren();

            if (clazz.isArray()) {
               for (int i = 0; i < Array.getLength(obj); i++) {
                   Array.set(obj, i, getElementValue(elements.get(i), clazz.getComponentType(), objectMap));
               }

           } else {
                for (Element fieldElement : elements) {
                    String fieldName = fieldElement.getAttributeValue("name");
                    String declaringClassName = fieldElement.getAttributeValue("declaringclass");

                    try {
                        Class<?> declaringClass = Class.forName(declaringClassName);
                        Field field = declaringClass.getDeclaredField(fieldName);

                        field.setAccessible(true);
                        field.set(obj, getElementValue(fieldElement, field.getType(), objectMap));

                    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
           }
        }
        
        // The root object is the first one in the map
        return objectMap.get(0);
    }

    private Object getElementValue(Element el, Class<?> clazz, Map<Integer, Object> objectMap)
    {
        String value = el.getText();
        if (el.getName().equals("value")) {
            if (clazz.equals(boolean.class))
                return Boolean.valueOf(value);
            else if (clazz.equals(byte.class))
                return Byte.valueOf(value);
            else if (clazz.equals(short.class))
                return Short.valueOf(value);
            else if (clazz.equals(int.class))
                return Integer.valueOf(value);
            else if (clazz.equals(long.class))
                return Long.valueOf(value);
            else if (clazz.equals(float.class))
                return Float.valueOf(value);
            else if (clazz.equals(double.class))
                return Double.valueOf(value);
            else if (clazz.equals(char.class))
                return value.charAt(0);
            else {
                return null;
            }

        } else 
            return objectMap.get(value);
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
                "  <object class=\"[Ljava.lang.String;\" id=\"3\" length=\"5\">\r\n" + //
		        "     <value>S</value>\r\n" + //
		        "     <value>m</value>\r\n" + //
		        "     <value>i</value>\r\n" + //
		        "     <value>t</value>\r\n" + //
		        "     <value>h</value>\r\n" + //
	            "  </object>\r\n" + //
                "</serialized>";
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
