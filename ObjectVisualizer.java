import ObjectPool.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.*;

public class ObjectVisualizer extends JFrame {

    private Map<Object, DefaultMutableTreeNode> objectMap = new HashMap<>();
    private JTree tree;

    public ObjectVisualizer(Object obj) {
        super("Object Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        tree = new JTree(new DefaultTreeModel(createTree(obj)));
        tree.setCellRenderer(new DefaultTreeCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
        setVisible(true);
    }

    private DefaultMutableTreeNode createTree(Object obj) {
        DefaultMutableTreeNode rootNode;

        if(Collection.class.isAssignableFrom(obj.getClass())) {
            rootNode = new DefaultMutableTreeNode("Received Objects:");
            try {
                Iterator<?> iter = ((Iterable<?>) obj).iterator();
                System.out.println(iter);
                while(iter.hasNext()) {
                    Object collectionObj = (Object) iter.next();

                    if(!objectMap.containsKey(collectionObj)) {
                        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(collectionObj.getClass().getName());
                        objectMap.put(collectionObj, childNode);
                        createObjectTree(collectionObj, collectionObj.getClass(), childNode); 
                        rootNode.add(childNode);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {    
            rootNode = new DefaultMutableTreeNode(obj.getClass().getName());
            objectMap.put(obj, rootNode);
            createObjectTree(obj, obj.getClass(), rootNode);
        }

        return rootNode;
    }

    private void createObjectTree(Object obj, Class<?> clazz, DefaultMutableTreeNode rootNode) {
        if (clazz == null) 
            return;
    
        rootNode.add(new DefaultMutableTreeNode("ID: " + Integer.toHexString(System.identityHashCode(obj))));
        
        if (clazz.isArray())                                        // handle Array Objects
            addArray(obj, clazz, rootNode);
        else if(Collection.class.isAssignableFrom(obj.getClass()))  // handle collections
            addCollection(obj, rootNode);
        else                                                        // handle simple objects
            addFields(obj, clazz, rootNode);
    }

    private void addCollection(Object obj, DefaultMutableTreeNode parentNode) {
        Iterator<?> iter = ((Iterable<?>) obj).iterator();
        int i = 0;

        while(iter.hasNext()) {
            Object collectionObj = (Object) iter.next();
            DefaultMutableTreeNode indexNode = new DefaultMutableTreeNode(parentNode.getUserObject()+"["+i+"]");
            parentNode.add(indexNode);
            i++;
            
            if (collectionObj == null) {
                indexNode.add(new DefaultMutableTreeNode("null"));
            } else {
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(collectionObj.getClass().getName());
                objectMap.put(collectionObj, childNode);
            
                createObjectTree(collectionObj, collectionObj.getClass(), childNode); 
                indexNode.add(childNode);
            } 
        }
        // Add length
        parentNode.add(new DefaultMutableTreeNode("Length: "+ Integer.toString(i)));
    }

    private void addArray(Object obj, Class<?> arr, DefaultMutableTreeNode node) {
        // Add component type
        Class<?> elType = arr.getComponentType();
        node.add(new DefaultMutableTreeNode("Component Type: " +elType.toString()));

        // Add length
        node.add(new DefaultMutableTreeNode("Length: "+Array.getLength(obj)));

        DefaultMutableTreeNode vNode = new DefaultMutableTreeNode("Values");
        node.add(vNode);

        for (int i = 0; i < Array.getLength(obj); i++) {
            DefaultMutableTreeNode indexNode = new DefaultMutableTreeNode(node.getUserObject()+"["+i+"]");
            vNode.add(indexNode);

            try {
                Object arrElement = Array.get(obj, i);

                if (arrElement == null)
                    indexNode.add(new DefaultMutableTreeNode("Value: null"));
                else {
                    indexNode.add(new DefaultMutableTreeNode("Type: "+arrElement.getClass().getTypeName()));

                    if (!elType.isPrimitive()) {
                        if(!objectMap.containsKey(arrElement)) {
                            objectMap.put(arrElement, indexNode);
                            createObjectTree(arrElement, arrElement.getClass(), indexNode);
                        } else 
                            indexNode.add(new DefaultMutableTreeNode("Reference to "+arrElement.toString()));
                        
                    } else 
                        indexNode.add(new DefaultMutableTreeNode("Value: "+arrElement.toString()));
                } 
   
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void addFields(Object obj, Class<?> clazz, DefaultMutableTreeNode parentNode) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Fields");
        parentNode.add(node);

        for(Field field : clazz.getDeclaredFields()) {
            Class<?> fType = field.getType();

            // Add field node
            DefaultMutableTreeNode fNode = new DefaultMutableTreeNode(field.getName());
            node.add(fNode);

            // Add field type
            fNode.add(new DefaultMutableTreeNode("Type: " + fType.toString()));

            try {
                field.setAccessible(true);
            } catch (Exception e) {
                fNode.add(new DefaultMutableTreeNode("Error unable to make field accessible"));
                return;
            }

            Object fieldObj = null;
            try {
                fieldObj= field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                return;
            }

            if (fType.isArray()) 
                addArray(fieldObj, fType, fNode);
            //else if(Collection.class.isAssignableFrom(fieldObj.getClass()))  
            //    addCollection(fieldObj, fNode);
            else {
                // Add field value
                if(fieldObj != null && !fType.isPrimitive()) {
                    if(!objectMap.containsKey(fieldObj)) {
                        objectMap.put(fieldObj, fNode);
                        createObjectTree(fieldObj, fieldObj.getClass(), fNode);

                    } else 
                        fNode.add(new DefaultMutableTreeNode("Reference to "+fieldObj.toString()));

                } else {
                    if (fieldObj == null)
                        fNode.add(new DefaultMutableTreeNode("Value: null"));
                    else
                        fNode.add(new DefaultMutableTreeNode("Value: " +fieldObj.toString()));
                }
            }
        }
    } 
 
    public void saveTree(String file) {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writeNodeToFile(rootNode, writer, "");
            System.out.println("Tree contents written to "+ file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeNodeToFile(DefaultMutableTreeNode node, BufferedWriter writer, String indent) throws IOException {
        writer.write(indent + node.getUserObject().toString() + "\n");

        Enumeration<?> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            writeNodeToFile(child, writer, indent + "  ");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimpleObject so = new SimpleObject(4);
            ArrayOfObjects objToInspect = null;
            
            try {
                objToInspect = new ArrayOfObjects(3);
                objToInspect.setObjectArrayElement(0, so);
                objToInspect.setObjectArrayElement(1, so);
                objToInspect.setObjectArrayElement(2, new SimpleObject(6));
            } catch (Exception e) {
                e.printStackTrace();
            }  

            new ObjectVisualizer(objToInspect).setVisible(true);
        });
    }
}
