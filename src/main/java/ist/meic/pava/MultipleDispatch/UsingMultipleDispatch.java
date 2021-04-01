package ist.meic.pava.MultipleDispatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class UsingMultipleDispatch {
    
    /** 
     * Invokes a given method using dynamic dispatch for the arguments.
     * 
     * @param receiver Receiver Object
     * @param name Receiver method name
     * @param args Arguments for the method
     * @return Object Method invocation
     */
    public static Object invoke(Object receiver, String name, Object... args) {
        try {
            Method method = bestMethod(receiver.getClass(), name,
                    Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
            return method.invoke(receiver, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static class Node {
        Class[] args;
        int[] level;

        Node(Class[] args, int[] level) {
            this.args = args;
            this.level = level;
        }
    }

    
    /** 
     * Generates new nodes from a given Node. The generated nodes are created by iterating the arguments and 
     * getting the Super Class of an argument at a time.
     * 
     * @param node Node to generate new nodes from
     * @return List<Node> List containing all generated Nodes
     */
    private static List<Node> generateNode(Node node) {
        List<Node> res = new LinkedList<>();
        for (int j = (node.args.length - 1); j >= 0; j--) {
            if (node.args[j] != Object.class && node.args[j].getSuperclass() != null) {
                Node newNode = new Node(node.args.clone(), node.level.clone());

                newNode.args[j] = newNode.args[j].getSuperclass();
                newNode.level[j]++;
                res.add(newNode);
            }
        }
        return res;
    }

    
    /** 
     * Finds the best method, the most specific, for the given arguments.
     * 
     * @param receiverType The type of the receiver Object
     * @param name The name of the receiver's method to be invoked
     * @param argType All the argument types
     * @return Method Most specific method for the given arguments
     * @throws NoSuchMethodException If there is no compatible method for the arguments
     */
    private static Method bestMethod(Class receiverType, String name, Class... argType) throws NoSuchMethodException {
        try {
            return receiverType.getMethod(name, argType);
        } catch (NoSuchMethodException e) {
            // Search in breadth -> left to right arguments
            Node initialNode = new Node(argType, new int[argType.length]);

            List<Node> visited = new LinkedList<>();
            List<Node> queue = new LinkedList<>(Collections.singleton(initialNode));

            Node currNode;
            visited.add(initialNode);

            while (!queue.isEmpty()) {
                currNode = queue.remove(0);

                List<Node> adjs = generateNode(currNode);

                for (Node next : adjs) {
                    if (visited.stream().noneMatch(node -> Arrays.equals(node.level, next.level))) {
                        try {
                            return receiverType.getMethod(name, next.args);
                        } catch (NoSuchMethodException exception) {
                            visited.add(next);
                            queue.add(next);
                        }
                    }
                }
            }

            throw e;
        }
    }
}