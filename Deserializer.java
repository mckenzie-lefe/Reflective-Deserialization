import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

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

        // To Do: Create objects and store them in the map
    }

}
