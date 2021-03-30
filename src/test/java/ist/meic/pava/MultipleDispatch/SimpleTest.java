package ist.meic.pava.MultipleDispatch;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class SimpleTest {
    static class Shape { }
    static class Line extends Shape { }
    static class Circle extends Shape { }

    static class Device {
        public String draw(Shape s) {
            return "draw what where?";
        }

        public String draw(Line l) {
            return "draw a line where?";
        }

        public String draw(Circle c) {
            return "draw a circle where?";
        }
    }

    static class Screen extends Device {
        public String draw(Shape s) {
            return "draw what on screen?";
        }

        public String draw(Line l) {
            return "drawing a line on screen!";
        }

        public String draw(Circle c) {
            return "drawing a circle on screen!";
        }
    }

    static class Printer extends Device {
        public String draw(Shape s) {
            return "draw what on screen?";
        }

        public String draw(Line l) {
            return "drawing a line on printer!";
        }

        public String draw(Circle c) {
            return "drawing a circle on printer!";
        }
    }

    @DataProvider
    private Object[][] drawOperations() {
        return new Object[][] {
                {new Screen(), new Line(), new Screen().draw(new Line())},
                {new Screen(), new Circle(), new Screen().draw(new Circle())},
                {new Printer(), new Line(), new Printer().draw(new Line())},
                {new Printer(), new Circle(), new Printer().draw(new Circle())},
        };
    }

    @Test(dataProvider = "drawOperations", description="OperationDraw")
    public void simpleTest(Device device, Shape shape, String result) {
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape), result);
    }

    @Test(dataProvider = "drawOperations", description="OperationDraw")
    public void simpleTestOnExtended(Device device, Shape shape, String result) {
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape), result);
    }
}

