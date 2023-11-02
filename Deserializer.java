import ObjectPool.*;
import org.jdom2.*;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class Deserializer {

    private Map<String, Object> objectMap;

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
                    Constructor constructor = objClass.getDeclaredConstructor(new Class[]{});
                    constructor.setAccessible(true);
                    obj = constructor.newInstance(new Object[] {});
                }
                pp("first obj: " + obj);
            } catch (ClassNotFoundException |  InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                //e.printStackTrace();
            }
            objectMap.put(objectElement.getAttributeValue("id"), obj);
        }

        // Second pass: Populate fields
        for (Element objectElement : objectElements) {
            
            Object object = objectMap.get(objectElement.getAttributeValue("id"));
            pp("\nPOP: " + object.getClass().getName());

            if (object == null)
                continue;

            Class<?> clazz = object.getClass();
            List<Element> elements = objectElement.getChildren();

            if (clazz.isArray()) {
                for (int i = 0; i < Array.getLength(object); i++) {
                    Array.set(object, i, getElementValue(elements.get(i), clazz.getComponentType(), objectMap));
                }

           } else {
                for (Element fieldElement : elements) {
                    String fieldName = fieldElement.getAttributeValue("name");
                    String declaringClassName = fieldElement.getAttributeValue("declaringclass");

                    try {
                        Field field = Class.forName(declaringClassName).getDeclaredField(fieldName);

                        field.setAccessible(true);
                        field.set(object, getElementValue(fieldElement.getChildren().get(0), field.getType(), objectMap));

                    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
           }
        }
        
        // The root object is the first one in the map
        return objectMap.get("0");
    }

    private Object getElementValue(Element el, Class<?> clazz, Map<String, Object> objectMap)
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
            else 
                return null;

        } else 
            return objectMap.get(value);  
    }

    public void pp(String m) {
        System.out.println(m);
    }

    public static void main(String[] args) {
        String xmlString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
            "<serialized>\r\n" + //
            "  <object class=\"ObjectPool.ArrayOfObjects\" id=\"0\">\r\n" + //
            "    <field name=\"simpleObjs\" declaringclass=\"ObjectPool.ArrayOfObjects\">\r\n" + //
            "      <reference>1</reference>\r\n" + //
            "    </field>\r\n" + //
            "  </object>\r\n" + //
            "  <object class=\"[LObjectPool.SimpleObject;\" id=\"1\" length=\"3\">\r\n" + //
            "    <reference>2</reference>\r\n" + //
            "    <reference>2</reference>\r\n" + //
            "    <reference>3</reference>\r\n" + //
            "  </object>\r\n" + //
            "  <object class=\"ObjectPool.SimpleObject\" id=\"2\">\r\n" + //
            "    <field name=\"primitiveInt\" declaringclass=\"ObjectPool.SimpleObject\">\r\n" + //
            "      <value>4</value>\r\n" + //
            "    </field>\r\n" + //
            "  </object>\r\n" + //
            "  <object class=\"ObjectPool.SimpleObject\" id=\"3\">\r\n" + //
            "    <field name=\"primitiveInt\" declaringclass=\"ObjectPool.SimpleObject\">\r\n" + //
            "      <value>6</value>\r\n" + //
            "    </field>\r\n" + //
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
