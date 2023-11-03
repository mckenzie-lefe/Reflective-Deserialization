package ObjectPool;

import java.util.ArrayList;

public class CollectionInstance {
    
    public ArrayList<Object> objectRefs;

    public CollectionInstance() {
        objectRefs = new ArrayList<Object>();
    }

    public void addReference(Object obj) {
        objectRefs.add(obj);
    }
}
