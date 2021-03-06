package ist.meic.pava.MultipleDispatchExtended;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class InterfacesClassesTest {

    public interface Shape {}
    public static class Line implements Shape {}
    public static class Circle implements Shape {}
    public static class Brush {}
    public static class Pencil extends Brush {}
    public static class Crayon extends Brush {}

    public static class Device {
        public String draw(Shape s, Brush b) {
            return "draw what where and with what?";
        }

        public String draw(Line l, Brush b) {
            return "draw a line where and with what?";
        }

        public String draw(Circle c, Brush b) {
            return "draw a circle where and with what?";
        }
    }

    public static class Screen extends Device {
        public String draw(Line l, Brush b) {
            return "draw a line where and with what?";
        }

        public String draw(Line l, Pencil p) {
            return "drawing a line on screen with pencil!";
        }

        public String draw(Line l, Crayon c) {
            return "drawing a line on screen with crayon!";
        }

        public String draw(Circle c, Brush b) {
            return "drawing a circle on screen with what?";
        }

        public String draw(Circle c, Pencil p) {
            return "drawing a circle on screen with pencil!";
        }
    }

    public static class Printer extends Device {
        public String draw(Line l, Brush b) {
            return "drawing a line on printer with what?";
        }

        public String draw(Circle c, Pencil p) {
            return "drawing a circle on printer with pencil!";
        }

        public String draw(Circle c, Crayon r) {
            return "drawing a circle on printer with crayon!";
        }
    }

    @DataProvider
    private Object[][] drawOperations() {
        return new Object[][] { 
                { new Screen(), new Line(), new Pencil(), new Screen().draw(new Line(), new Pencil()) },
                { new Screen(), new Line(), new Crayon(), new Screen().draw(new Line(), new Crayon()) },
                { new Screen(), new Circle(), new Pencil(), new Screen().draw(new Circle(), new Pencil()) },
                { new Screen(), new Circle(), new Crayon(), new Screen().draw(new Circle(), new Crayon()) },
                { new Printer(), new Line(), new Pencil(), new Printer().draw(new Line(), new Pencil()) },
                { new Printer(), new Line(), new Crayon(), new Printer().draw(new Line(), new Crayon()) },
                { new Printer(), new Circle(), new Pencil(), new Printer().draw(new Circle(), new Pencil()) },
                { new Printer(), new Circle(), new Crayon(), new Printer().draw(new Circle(), new Crayon()) }, };
    }

    @Test(dataProvider = "drawOperations", description = "OperationDraw")
    public void interfacesClassesTestExtended(Device device, Shape shape, Brush brush, String result) {
        assertEquals(UsingMultipleDispatch.invoke(device, "draw", shape, brush), result);
    }
}
