import ObjectPool.*;
import java.util.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.lang.reflect.*;

public class ObjectVisualizer extends JFrame {

    private Map<Object, DefaultMutableTreeNode> objectMap = new HashMap<>();

    public ObjectVisualizer(Object obj) {
        super("Object Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        DefaultMutableTreeNode rootNode;

        if(Collection.class.isAssignableFrom(obj.getClass())) {
            rootNode = new DefaultMutableTreeNode("Objects");
            try {
                Iterator<?> iter = ((Iterable<?>) obj).iterator();
                System.out.println(iter);
                while(iter.hasNext()) {
                    Object collectionObj = (Object) iter.next();
                    
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(collectionObj.getClass().getName());
                    objectMap.put(collectionObj, childNode);
                    createObjectTree(collectionObj, collectionObj.getClass(), childNode); 
                    rootNode.add(childNode);
                    
                    objectMap.clear();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("normal");
            
            rootNode = new DefaultMutableTreeNode(obj.getClass().getName());
            objectMap.put(obj, rootNode);
            createObjectTree(obj, obj.getClass(), rootNode);
            
        }
        
        JTree tree = new JTree(new DefaultTreeModel(rootNode));
        tree.setCellRenderer(new DefaultTreeCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
    }

    private void createObjectTree(Object obj, Class<?> clazz, DefaultMutableTreeNode rootNode) {
        if (clazz == null) {
            System.out.println("Class is null");
            return;
        }

        // handle Array Objects
        if (clazz.isArray()) 
            addArrayField(obj, clazz, rootNode);
        else {
            rootNode.add(new DefaultMutableTreeNode("ID: " + Integer.toHexString(System.identityHashCode(obj))));

            // Add inheritance nodes
            //addInheritance(clazz, rootNode);
            
            // Add method nodes
            //createNodes("Methods", clazz.getDeclaredMethods(), rootNode);

            // Add constructor nodes
            //createNodes("Constructors", clazz.getDeclaredConstructors(), rootNode);

            // Add field nodes
            addFields(obj, clazz, rootNode);
        }
    }

    private void addArrayField(Object obj, Class<?> arr, DefaultMutableTreeNode node) {
        // Add component type
        node.add(new DefaultMutableTreeNode("Component Type: " +arr.getComponentType().toString()));

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

                    if (!arrElement.getClass().isPrimitive()) {
                        if(!objectMap.containsKey(arrElement)) {
                            objectMap.put(arrElement, indexNode);
                            createObjectTree(arrElement, arrElement.getClass(), indexNode);
                        } else {
                            indexNode.add(new DefaultMutableTreeNode("Reference to "+arrElement.toString()));
                        }
                    }  
                    else 
                        indexNode.add(new DefaultMutableTreeNode("Value: "+arrElement.toString()));
                } 
   
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    private void addInheritance(Class<?> clazz, DefaultMutableTreeNode node) {
        DefaultMutableTreeNode iNode = new DefaultMutableTreeNode("Inheritance");
        node.add(iNode);
        iNode.add(new DefaultMutableTreeNode("extends "+ clazz.getSuperclass()));

        for(Class<?> inter : clazz.getInterfaces()) {
            iNode.add(new DefaultMutableTreeNode("implements " +inter.getName().toString()));
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

            if (fType.isArray()) {
                addArrayField(fieldObj, fType, fNode);
            } else {
                // Add field value
                if(fieldObj != null && !fType.isPrimitive()) {
                    if(!objectMap.containsKey(fieldObj)) {
                        objectMap.put(fieldObj, fNode);
                        createObjectTree(fieldObj, fieldObj.getClass(), fNode);

                    } else 
                        fNode.add(new DefaultMutableTreeNode("Reference to "+fieldObj.toString()));

                }
                else {
                    if (fieldObj == null)
                        fNode.add(new DefaultMutableTreeNode("Value: null"));
                    else
                        fNode.add(new DefaultMutableTreeNode("Value: " +fieldObj.toString()));
                }
            }
        }
    } 

    private void createNodes(String nodeName, AccessibleObject[] members, DefaultMutableTreeNode parentNode) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(nodeName);
        parentNode.add(childNode);

        for (AccessibleObject member : members) {
            childNode.add(new DefaultMutableTreeNode(member.toString()));
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
