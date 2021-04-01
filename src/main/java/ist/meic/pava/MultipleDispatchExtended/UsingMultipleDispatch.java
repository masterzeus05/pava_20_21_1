package ist.meic.pava.MultipleDispatchExtended;

import java.lang.reflect.Array;
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

    
    /** 
     * Generates new nodes from a given Node. The generated nodes are created by iterating the arguments and 
     * getting the Super Class or the Interface of an argument at a time.
     * 
     * @param node Node to generate new nodes from
     * @return List<Node> List containing all generated Nodes
     */
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

    
    /** 
     * Verifies if boxing or unboxing can be performed.
     * 
     * @param methodArg Method's argument class
     * @param argType Class of the argument being invoked
     * @return boolean If wether or not boxing or unboxing can be performed
     */
    private static boolean checkBoxingAndUnboxing(Class methodArg, Class argType) {
        return (methodArg.isPrimitive() && !argType.isPrimitive() && Array.get(Array.newInstance(methodArg, 1), 0).getClass() == argType) || 
            (!methodArg.isPrimitive() && argType.isPrimitive() && Array.get(Array.newInstance(argType, 1), 0).getClass() == methodArg);
    }

    
    /** 
     * Verifies if the it is not possible to assign the method's argument to the given one, and if the
     * boxing and unboxing are also not possible.
     * 
     * @param methodArg Method's argument class
     * @param argType Class of the argument being invoked
     * @return boolean True if it is not assignable and the boxing is impossible, false otherwise
     */
    private static boolean notAssignable(Class methodArg, Class argType) {
        return !methodArg.isAssignableFrom(argType) && !checkBoxingAndUnboxing(methodArg, argType);
    }

    
    /** 
     * Verifies if the it the method's argument is not equals and the boxing and unboxing are not possible
     * for the given argument.
     * 
     * @param methodArg
     * @param argType
     * @return boolean True if it is not equal and the boxing is impossible, false otherwise
     */
    private static boolean notEquals(Class methodArg, Class argType) {
        return !methodArg.equals(argType) && !checkBoxingAndUnboxing(methodArg, argType);
    }

    
    /** 
     * Finds the possible methods for the given arguments and method name in the provided 
     * array of methods.
     * 
     * @param initialMethods Array containing the methods to be filtered
     * @param methodName Method name to be found
     * @param argType Arguments for the chosen method name
     * @return Method[] Array containing all the matches
     */
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

    
    /** 
     * Finds the best method given a array of methods with the same name and arguments.
     * 
     * @param methods Array containing methods with the same name
     * @param argType Arguments' classes for the desired method
     * @return Method Most specific method in the given array
     * @throws NoSuchMethodException Thrown if there is no compatible method
     */
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

    
    /** 
     * Finds the most specific method given the receiver type, the name of the method
     * and the arguments' type.
     * 
     * @param receiverType The class of the receiver type
     * @param name The name of the method
     * @param argType Type of the arguments
     * @return Method Most specific method
     * @throws NoSuchMethodException Thrown if there is no method compatible with the given arguments
     */
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