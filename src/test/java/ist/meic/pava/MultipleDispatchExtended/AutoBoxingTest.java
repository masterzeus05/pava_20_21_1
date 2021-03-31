package ist.meic.pava.MultipleDispatchExtended;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AutoBoxingTest {
    static class Shape { }
    static class Line extends Shape { }
    static class Circle extends Shape { }

    static class Device {
        public String draw(Shape s, int args) {
            return "draw what where? with " + args;
        }

        public String draw(Line l, int args) {
            return "draw a line where? with " + args;
        }

        public String draw(Circle c, int args) {
            return "draw a circle where? with " + args;
        }
    }

    static class Screen extends Device {
        public String draw(Shape s, int args) {
            return "draw what on screen? with " + args;
        }

        public String draw(Line l, int args) {
            return "drawing a line on screen! with " + args;
        }

        public String draw(Circle c, int args) {
            return "drawing a circle on screen! with " + args;
        }
    }

    static class Printer extends Device {
        public String draw(Shape s, int args) {
            return "draw what on screen? with " + args;
        }

        public String draw(Line l, int args) {
            return "drawing a line on printer! with " + args;
        }

        public String draw(Circle c, int args) {
            return "drawing a circle on printer! with " + args;
        }
    }

    @DataProvider
    private Object[][] drawOperations() {
        return new Object[][] {
                {new Screen(), new Line(), new Screen().draw(new Line(), 0)},
                {new Screen(), new Circle(), new Screen().draw(new Circle(), 0)},
                {new Printer(), new Line(), new Printer().draw(new Line(), 0)},
                {new Printer(), new Circle(), new Printer().draw(new Circle(), 0)},
        };
    }

    @Test(dataProvider = "drawOperations", description="OperationDraw")
    public void boxingExtended(Device device, Shape shape, String result) {
        assertEquals(UsingMultipleDispatch.invoke(device,"draw", shape, 0), result);
    }
}

