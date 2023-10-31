import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        for (Element objectElement : objectElements) {
            int objectId = Integer.parseInt(objectElement.getAttributeValue("id"));
            Object obj = objectMap.get(objectId);
            
            // TO DO: Populate object fields
        }
        
        // The root object is the first one in the map
        return objectMap.get(0);

    }
}
