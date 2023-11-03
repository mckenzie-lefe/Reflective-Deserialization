package ObjectPool;

public class ArrayOfObjects{
    
    public SimpleObject[] simpleObjs;

    public ArrayOfObjects() { }

    public ArrayOfObjects(int size) {
        simpleObjs = new SimpleObject[size];
    }

    public void setObjectArrayElement(int index, SimpleObject obj) {
        simpleObjs[index] = obj;
    }
}
