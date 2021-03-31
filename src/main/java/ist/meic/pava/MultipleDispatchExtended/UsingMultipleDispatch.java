package ist.meic.pava.MultipleDispatchExtended;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class UsingMultipleDispatch {
    public static Object invoke(Object receiver, String name, Object... args) {
        try {
            Method method = bestMethod(receiver.getClass(), name,
                    Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
            // Prepare args if method is varArgs
            if (method.isVarArgs()) {
                // Find index where varArgs starts
                Class[] params = method.getParameterTypes();
                int varArgIndex = params.length - 1;

                // Prepare args
                List<Object> finalArgs = new LinkedList<>();
                Object varArgs = Array.newInstance(params[varArgIndex].getComponentType(), args.length - varArgIndex);

                for (int k = 0; k < varArgIndex; k++) {
                    finalArgs.add(args[k]);
                }
                for (int j = varArgIndex; j < args.length; j++) {
                    Array.set(varArgs, j - varArgIndex, args[j]);
                }
                finalArgs.add(varArgs);
                args = finalArgs.toArray();
            }

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
            if (node.args[j] != Object.class && node.args[j].getSuperclass() != null) {
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

    private static boolean checkBoxingAndUnboxing(Class methodParam, Class argType) {
        return (methodParam.isPrimitive() && !argType.isPrimitive() && Array.get(Array.newInstance(methodParam, 1), 0).getClass() == argType) || 
            (!methodParam.isPrimitive() && argType.isPrimitive() && Array.get(Array.newInstance(argType, 1), 0).getClass() == methodParam);
    }

    private static boolean notAssignable(Class methodParam, Class argType) {
        return !methodParam.isAssignableFrom(argType) && !checkBoxingAndUnboxing(methodParam, argType);
    }

    private static boolean notEquals(Class methodParam, Class argType) {
        return !methodParam.equals(argType) && !checkBoxingAndUnboxing(methodParam, argType);
    }

    private static Method[] filterMethods(Method[] initialMethods, String methodName, Class... argType) {
        return Arrays.stream(initialMethods).filter(m -> {
            if (!m.getName().equals(methodName)) return false;
            Class[] params = m.getParameterTypes();
            if (m.isVarArgs()) {
                // the VarArgs parameter is only at the end, and every one after the last varArg must be the same type as the VarArgs
                // Check until varArg parameter if everything is assignable
                int i = 0;
                for (; i < (params.length - 1); i++) {
                    if (notAssignable(params[i], argType[i])) return false;
                }

                // Check if last parameter is varArg
                if (!params[i].isArray()) return false;

                Class varArgType = params[i].getComponentType();
                // Check if remaining given arguments are of this component type
                for (int j = i; j < argType.length; j++) {
                    if (notAssignable(varArgType, argType[j])) return false;
                }
            } else {

                // Check if length of given parameters equals the method parameters
                if (params.length != argType.length) return false;

                // For each parameter check if we can assign from the given types
                for (int i = 0; i < params.length; i++) {
                    if (notAssignable(params[i], argType[i])) return false;
                }
            }
            return true;
        }).toArray(Method[]::new);
    }

    private static Method matchMethod(Method[] methods, Class... argType) throws NoSuchMethodException {
        return Arrays.stream(methods).filter(m -> {
            Class[] params = m.getParameterTypes();
            if (m.isVarArgs()) {
                // the VarArgs parameter is only at the end, and every one after the last varArg must be the same type as the VarArgs
                // Check until varArg parameter if everything is assignable
                int i = 0;
                for (; i < (params.length - 1); i++) {
                    if (notEquals(params[i], argType[i])) return false;
                }

                // Check if last parameter is varArg
                if (!params[i].isArray()) return false;

                Class varArgType = params[i].getComponentType();
                // Check if remaining given arguments are of this component type
                for (int j = i; j < argType.length; j++) {
                    if (notEquals(varArgType, argType[j])) return false;
                }
            } else {

                // Check if length of given parameters equals the method parameters
                if (params.length != argType.length) return false;

                // For each parameter check if we can assign from the given types
                for (int i = 0; i < params.length; i++) {
                    if (notEquals(params[i], argType[i])) return false;
                }
            }
            return true;
        }).findFirst().orElseThrow(NoSuchMethodException::new);
    }

    private static Method bestMethod(Class receiverType, String name, Class... argType) throws NoSuchMethodException {
        Method[] methods = filterMethods(receiverType.getMethods(), name, argType);
        try {
            return matchMethod(methods, argType);
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
                            return matchMethod(methods, next.args);
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