import ObjectPool.*;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.lang.reflect.*;
import java.util.*;

import javax.swing.SwingUtilities;

public class Deserializer {

    private Map<String, Object> objectMap;

    public Deserializer() {
        objectMap = new HashMap<>();
    }

    public Object deserialize(Document document) {
        
        Element rootElement = document.getRootElement();
        List<Element> objectElements = rootElement.getChildren("object");

        // Create objects & store them in the map
        createObjectInstances(objectElements);
        
        // Populate object fields
        populateFields(objectElements);

        List<Object> ol = new ArrayList<>();
        for(Map.Entry<String, Object> e : objectMap.entrySet()) {
            ol.add(e.getValue());
        }

        //return objectMap.get("0");
        return ol;
    }

    private void populateFields(List<Element> objectElements) {
        if (objectElements == null) 
            return;
        
        for (Element objectElement : objectElements) {
            Object object = objectMap.get(objectElement.getAttributeValue("id"));
        
            if (object == null)
                continue;

            System.out.println("\nPopulate: " + object.getClass().getName());

            Class<?> clazz = object.getClass();
            List<Element> elements = objectElement.getChildren();

            if (elements.size() == 0)
                return;

            if (clazz.isArray()) {
                for (int i = 0; i < Array.getLength(object); i++) {
                    Array.set(object, i, getElementValue(elements.get(i), clazz.getComponentType(), objectMap));
                }

            } else if (Collection.class.isAssignableFrom(object.getClass())) {
                for (int i = 0; i < Integer.valueOf(objectElement.getAttributeValue("length")); i++) {
                    try {
                        // Find the add method of the collection class
                        Method addMethod = Collection.class.getDeclaredMethod("add", Object.class);
                        addMethod.setAccessible(true);
                        addMethod.invoke(object, getElementValue(elements.get(i), elements.get(i).getClass(), objectMap));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
            } else {
                for (Element fieldElement : elements) {
                    String fieldName = fieldElement.getAttributeValue("name");
                    String declaringClassName = fieldElement.getAttributeValue("declaringclass");

                    try {
                        Field field = Class.forName(declaringClassName).getDeclaredField(fieldName);

                        field.setAccessible(true);
                        List<Element> fieldContent = fieldElement.getChildren();

                        if (fieldContent.size() == 0)   // field is null
                            field.set(object, null);
                        else
                            field.set(object, getElementValue(fieldContent.get(0), field.getType(), objectMap));

                    } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void createObjectInstances(List<Element> objectElements) {
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

            } catch (ClassNotFoundException |  InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            objectMap.put(objectElement.getAttributeValue("id"), obj);
        }
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
            

        String str2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + //
            "<serialized>\r\n" + //
            "  <object class=\"ObjectPool.CircularReference\" id=\"0\">\r\n" + //
            "    <field name=\"circularRef\" declaringclass=\"ObjectPool.CircularReference\">\r\n" + //
            "      <reference>1</reference>\r\n" + //
            "    </field>\r\n" + //
            "  </object>\r\n" + //
            "  <object class=\"ObjectPool.CircularReference\" id=\"1\">\r\n" + //
            "    <field name=\"circularRef\" declaringclass=\"ObjectPool.CircularReference\">\r\n" + //
            "      <reference>0</reference>\r\n" + //
            "    </field>\r\n" + //
            "  </object>\r\n" + //
            "</serialized>";


        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            Document document = saxBuilder.build(new StringReader(str2));

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
