package ist.meic.pava.MultipleDispatchExtended;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AutoBoxingTest {
    static class Shape { }
    static class Line extends Shape { }
    static class Circle extends Shape { }
    static class Rectangle extends Shape { }
    static class Square extends Shape { }
    static class Triangle extends Shape { }

    static class Device {
        public String draw(Shape s, int args) {
            return "draw what where? with " + args;
        }

        public String draw(Line l, double args) {
            return "draw a line where? with " + args;
        }

        public String draw(Circle c, int args) {
            return "draw a circle where? with " + args;
        }

        public String draw(Rectangle c, long args) {
            return "draw a rectangle where? with " + args;
        }

        public String draw(Square c, Short args) {
            return "draw a square where? with " + args;
        }

        public String draw(Triangle c, Integer... args) {
            return "draw a triangle where? with " + args.length;
        }
    }

    static class Screen extends Device {
        public String draw(Shape s, int args) {
            return "draw what on screen? with " + args;
        }

        public String draw(Line l, double args) {
            return "drawing a line on screen! with " + args;
        }

        public String draw(Circle c, int args) {
            return "drawing a circle on screen! with " + args;
        }

        public String draw(Rectangle c, long args) {
            return "draw a rectangle on screen! with " + args;
        }

        public String draw(Square c, Short args) {
            return "draw a square on screen! with " + args;
        }

        public String draw(Triangle c, Integer... args) {
            return "draw a triangle on screen! with " + args.length;
        }
    }

    static class Printer extends Device {
        public String draw(Shape s, int args) {
            return "draw what on screen? with " + args;
        }

        public String draw(Line l, double args) {
            return "drawing a line on printer! with " + args;
        }

        public String draw(Circle c, int args) {
            return "drawing a circle on printer! with " + args;
        }

        public String draw(Rectangle c, long args) {
            return "draw a rectangle on printer! with " + args;
        }

        public String draw(Square c, Short args) {
            return "draw a square on printer! with " + args;
        }

        public String draw(Triangle c, Integer... args) {
            return "draw a triangle on printer! with " + args.length;
        }
    }

    @DataProvider
    private Object[][] drawOperationsInt() {
        return new Object[][] {
                {new Screen(), new Circle(), new Screen().draw(new Circle(), 0)},
                {new Printer(), new Circle(), new Printer().draw(new Circle(), 0)},
        };
    }

    @Test(dataProvider = "drawOperationsInt", description="OperationDraw")
    public void boxingExtendedInt(Device device, Shape shape, String result) {
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape, Integer.valueOf(0)), result);
    }

    @DataProvider
    private Object[][] drawOperationsDouble() {
        return new Object[][] {
                {new Screen(), new Line(), new Screen().draw(new Line(), 3.43)},
                {new Printer(), new Line(), new Printer().draw(new Line(), 3.43)},
        };
    }

    @Test(dataProvider = "drawOperationsDouble", description="OperationDraw")
    public void boxingExtendedDouble(Device device, Shape shape, String result) {
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape, Double.valueOf(3.43)), result);
    }

    @DataProvider
    private Object[][] drawOperationsLong() {
        long value = 0;
        return new Object[][] {
                {new Screen(), new Rectangle(), new Screen().draw(new Rectangle(), value)},
                {new Printer(), new Rectangle(), new Printer().draw(new Rectangle(), value)},
        };
    }

    @Test(dataProvider = "drawOperationsLong", description="OperationDraw")
    public void boxingExtendedLong(Device device, Shape shape, String result) {
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape, Long.valueOf(0)), result);
    }

    @DataProvider
    private Object[][] drawOperationsShort() {
        return new Object[][] {
                {new Screen(), new Square(), new Screen().draw(new Square(), Short.valueOf("0"))},
                {new Printer(), new Square(), new Printer().draw(new Square(), Short.valueOf("0"))},
        };
    }

    @Test(dataProvider = "drawOperationsShort", description="OperationDraw")
    public void boxingExtendedShort(Device device, Shape shape, String result) {
        short value = 0;
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape, value), result);
    }

    @DataProvider
    private Object[][] drawOperationsVarArgs() {
        Integer val = 0;
        return new Object[][] {
                {new Screen(), new Triangle(), new Screen().draw(new Triangle(), val, val, val)},
                {new Printer(), new Triangle(), new Printer().draw(new Triangle(), val, val, val)},
        };
    }

    @Test(dataProvider = "drawOperationsVarArgs", description="OperationDraw")
    public void boxingExtendedVarArgs(Device device, Shape shape, String result) {
        int val = 0;
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape, val, val, val), result);
    }
}

