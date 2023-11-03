package ObjectPool;

public class CircularReference {
    public CircularReference circularRef;

    public CircularReference() {
        circularRef = null;
    }

    public CircularReference(CircularReference obj) {
        circularRef = obj;
    }

    public void setCircularReference(CircularReference obj) {
        circularRef = obj;
    }
}
