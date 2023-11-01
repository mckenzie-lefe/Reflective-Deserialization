import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;

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

        // Add interface nodes
        rootNode.add(createNodes("Interfaces", getInterfaces(clazz), Color.BLUE));
    
        // Add superclass nodes
        rootNode.add(createNodes("Superclass", getSuperclass(clazz), Color.RED));

        // Add field nodes
        DefaultMutableTreeNode fieldsNode = new DefaultMutableTreeNode("Fields");
        rootNode.add(fieldsNode);
        getFields(obj, clazz, fieldsNode);

        // Add method nodes
        rootNode.add(createNodes("Methods", getMethods(clazz), Color.ORANGE));

        // Add constructor nodes
        rootNode.add(createNodes("Constructors", getConstructors(clazz), Color.YELLOW));

    }

    private List<String> getInterfaces(Class<?> clazz) {
        List<String> iNames =  new ArrayList<String>();
        for(Class<?> inter : clazz.getInterfaces()) {
            iNames.add(inter.getName());
        }
        return iNames;
    } 

    private List<String> getSuperclass(Class<?> clazz) {
        List<String> sNames =  new ArrayList<String>();
        sNames.add(clazz.getSuperclass().getName());
        return sNames;
    } 

    private List<String> getConstructors(Class<?> clazz) {
        List<String> cNames =  new ArrayList<String>();
        for(Constructor<?> c : clazz.getDeclaredConstructors()) {
            cNames.add(c.getName());
        }
        return cNames;
    } 
    
    private void getFields(Object obj, Class<?> clazz, DefaultMutableTreeNode node) {
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

    private List<String> getMethods(Class<?> clazz) {
        List<String> mNames =  new ArrayList<String>();
        for(Method method: clazz.getDeclaredMethods()) {
            mNames.add(method.getName());
        }
        return mNames;
    } 

    private DefaultMutableTreeNode createNodes(String nodeName, List<String> members, Color color) {
        DefaultMutableTreeNode parentNode = new DefaultMutableTreeNode(nodeName);
        for (String member : members) {
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(member);
            parentNode.add(node);
        }
        return parentNode;
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
                } else if (nodeName.equals("Interfaces")) {
                    setBackgroundNonSelectionColor(new Color(0,182,255)); //blue
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (nodeName.equals("Superclass")) {
                    setBackgroundNonSelectionColor(new Color(255,71,71)); //red
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (nodeName.equals("Type")) {
                    setBackgroundNonSelectionColor(new Color(171,50,252)); // purple
                    setFont(getFont().deriveFont(Font.BOLD));
                } else if (nodeName.equals("Value")) {
                    setBackgroundNonSelectionColor(new Color(0,255, 250)); // light blue
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    setBackgroundNonSelectionColor(new Color(255,255, 255)); //white
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
                objToInspect = new ClassB();
            } catch (Exception e) {
                e.printStackTrace();
            }  // Replace with your actual object
            new ObjectVisualizer(objToInspect).setVisible(true);
        });
    }
}
