package ist.meic.pava.MultipleDispatch;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class VarArgsTest {
    static class Shape { }
    static class Line extends Shape { }
    static class Circle extends Shape { }

    static class Device {
        public String draw(Shape s, int... args) {
            return "draw what where? with " + args.length;
        }

        public String draw(Line l, int... args) {
            return "draw a line where? with " + args.length;
        }

        public String draw(Circle c, int... args) {
            return "draw a circle where? with " + args.length;
        }
    }

    static class Screen extends Device {
        public String draw(Shape s, int... args) {
            return "draw what on screen? with " + args.length;
        }

        public String draw(Line l, int... args) {
            return "drawing a line on screen! with " + args.length;
        }

        public String draw(Circle c, int... args) {
            return "drawing a circle on screen! with " + args.length;
        }
    }

    static class Printer extends Device {
        public String draw(Shape s, int... args) {
            return "draw what on screen? with " + args.length;
        }

        public String draw(Line l, int... args) {
            return "drawing a line on printer! with " + args.length;
        }

        public String draw(Circle c, int... args) {
            return "drawing a circle on printer! with " + args.length;
        }
    }

    @DataProvider
    private Object[][] drawOperations() {
        return new Object[][] {
                {new Screen(), new Line(), new Screen().draw(new Line(), 0, 1, 2, 3)},
                {new Screen(), new Circle(), new Screen().draw(new Circle(), 0, 1, 2, 3)},
                {new Printer(), new Line(), new Printer().draw(new Line(), 0, 1, 2, 3)},
                {new Printer(), new Circle(), new Printer().draw(new Circle(), 0, 1, 2, 3)},
        };
    }

    // @Test(dataProvider = "drawOperations", description="OperationDraw")
    // public void varArgsTestExtended(Device device, Shape shape, String result) {
    //     assertEquals(UsingMultipleDispatchExtended.invoke(device,"draw", shape, 0, 1, 2, 3), result);
    // }
}

