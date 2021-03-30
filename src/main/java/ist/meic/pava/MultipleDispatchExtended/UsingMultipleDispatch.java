package ist.meic.pava.MultipleDispatchExtended;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class UsingMultipleDispatch {
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

    private static List<Node> generateNode(Node node) {
        List<Node> res = new LinkedList<>();
        for (int j = (node.args.length - 1); j >= 0; j--) {
            if (node.args[j] != Object.class) {
                Class currClass = node.args[j];
                Node newNode = new Node(node.args.clone(), node.level.clone());

                newNode.args[j] = newNode.args[j].getSuperclass();
                newNode.level[j]++;

                // Get interfaces
                for (Class currInterface : currClass.getInterfaces()) {
                    Node interNode = new Node(node.args.clone(), node.level.clone());

                    interNode.args[j] = currInterface;
                    interNode.level[j]++;
                    res.add(interNode);
                }

                res.add(newNode);
            }
        }
        return res;
    }

    private static Method bestMethod(Class receiverType, String name, Class... argType) throws NoSuchMethodException {
        try {
            return receiverType.getMethod(name, argType);
        } catch (NoSuchMethodException e) {
            // Search in breadth -> right to left arguments
            Node initialNode = new Node(argType, new int[argType.length]);

            List<Node> visited = new LinkedList<>();
            List<Node> queue = new LinkedList<>(Collections.singleton(initialNode));

            Node currNode;
            visited.add(initialNode);

            while (!queue.isEmpty()) {
                currNode = queue.remove(0);

                List<Node> adjs = generateNode(currNode);
                // System.out.println(Arrays.toString(adjs.stream().map(n -> Arrays.toString(n.level)).toArray()));

                for (Node next : adjs) {
                    if (visited.stream().noneMatch(node -> Arrays.equals(node.level, next.level))) {
                        try {
                            return receiverType.getMethod(name, next.args);
                        } catch (NoSuchMethodException exception) {
                            visited.add(next);
                            queue.add(next);
                        }

                        visited.add(next);
                        queue.add(next);
                    }
                }
            }

            throw e;
        }
    }
}