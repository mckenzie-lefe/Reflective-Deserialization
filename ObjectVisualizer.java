import java.util.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;
import java.lang.reflect.*;

public class ObjectVisualizer extends JFrame {

    public ObjectVisualizer(Object obj) {
        super("Object Visualizer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(obj.getClass().getName());
        createObjectTree(obj, obj.getClass(), rootNode);
        JTree tree = new JTree(new DefaultTreeModel(rootNode));
        tree.setCellRenderer(new CustomTreeCellRenderer());

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
    }

    private void createObjectTree(Object obj, Class<?> clazz, DefaultMutableTreeNode rootNode) {
        if (clazz == null) {
            System.out.println("Class is null");
            return;
        }

        // handle Array Objects
        if (clazz.isArray()) {
            addArrayField(obj, clazz, rootNode);
        } else {
            // Add inheritance nodes
            addInheritance(clazz, rootNode);

            // Add method nodes
            createNodes("Methods", clazz.getDeclaredMethods(), rootNode);

            // Add constructor nodes
            createNodes("Constructors", clazz.getDeclaredConstructors(), rootNode);

            // Add field nodes
            addFields(obj, clazz, rootNode);
        }
    }


    private void addArrayField(Object obj, Class<?> arr, DefaultMutableTreeNode node) {
        // Add component type
        DefaultMutableTreeNode ctNode = new DefaultMutableTreeNode("Component Type");
        node.add(ctNode);
        ctNode.add(new DefaultMutableTreeNode(arr.getComponentType().toString()));

        // Add length
        node.add(new DefaultMutableTreeNode("Length: "+Array.getLength(obj)));

        DefaultMutableTreeNode vNode = new DefaultMutableTreeNode("Values");
        node.add(vNode);
        for (int i = 0; i < Array.getLength(obj); i++) {
            DefaultMutableTreeNode indexNode = new DefaultMutableTreeNode("["+i+"]");
            vNode.add(indexNode);
            DefaultMutableTreeNode valNode = new DefaultMutableTreeNode("Value");
            indexNode.add(valNode);
            try {
                Object arrElement = Array.get(obj, i);

                if (arrElement == null)
                    valNode.add(new DefaultMutableTreeNode("null"));
                else {
                    DefaultMutableTreeNode tNode = new DefaultMutableTreeNode("Type");
                    indexNode.add(tNode);
                    tNode.add(new DefaultMutableTreeNode(arrElement.getClass().getTypeName()));

                    if (!arrElement.getClass().isPrimitive())
                        createObjectTree(arrElement, arrElement.getClass(), valNode);
                    else 
                        valNode.add(new DefaultMutableTreeNode(arrElement.toString()));
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
            DefaultMutableTreeNode tNode = new DefaultMutableTreeNode("Type");
            fNode.add(tNode);
            tNode.add(new DefaultMutableTreeNode(fType.toString()));

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
            }

            // Add field value
            if(fieldObj != null && !fType.isPrimitive()) {
                createObjectTree(fieldObj, fieldObj.getClass(), fNode);
            }
            else {
                DefaultMutableTreeNode vNode = new DefaultMutableTreeNode("Value");
                fNode.add(vNode);
                if (fieldObj == null)
                    vNode.add(new DefaultMutableTreeNode("null"));
                else
                    vNode.add(new DefaultMutableTreeNode(fieldObj.toString()));
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

    private static class CustomTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                      boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;

            if (node.getUserObject() instanceof String) {
                String nodeName = (String) node.getUserObject();
                if (nodeName.equals("Fields")) {
                    setBackgroundNonSelectionColor(new Color(0,234,23)); // green
                } else if (nodeName.equals("Methods")) {
                    setBackgroundNonSelectionColor(new Color(255,114,0));  // orange
                } else if (nodeName.equals("Constructors")) {
                    setBackgroundNonSelectionColor(Color.YELLOW);
                } else if (nodeName.equals("Inheritance")) {
                    setBackgroundNonSelectionColor(new Color(0,182,255)); // blue
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (nodeName.equals("Type")) {
                    setBackgroundNonSelectionColor(new Color(171,50,252)); // purple
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (nodeName.equals("Component Type")) {
                    setBackgroundNonSelectionColor(new Color(180, 138,252)); // lightpurple
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (nodeName.equals("Value") || nodeName.equals("Values")) {
                    setBackgroundNonSelectionColor(new Color(0,255, 250)); // light blue
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (nodeName.contains("Length: ")) {
                    setBackgroundNonSelectionColor(new Color(252, 138, 195)); // pink
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackgroundNonSelectionColor(new Color(255,255, 255)); // white
                    setFont(getFont().deriveFont(Font.BOLD));
                }
            }

            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Object objToInspect = null;
            try {
                objToInspect = new ClassD();
            } catch (Exception e) {
                e.printStackTrace();
            }  // Replace with your actual object
            new ObjectVisualizer(objToInspect).setVisible(true);
        });
    }
}
