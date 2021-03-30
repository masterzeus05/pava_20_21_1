package ist.meic.pava.MultipleDispatch;

/**
 * Hello world!
 *
 */
public class App 
{

    public static class Shape {}
    public static class Line extends Shape {}
    public static class Circle extends Shape {}
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

    public static void main( String[] args )
    {
        Device[] devices = new Device[] { new Screen(), new Printer() };
        Shape[] shapes = new Shape[] { new Line(),new Circle() };
        Brush[] brushes = new Brush[] {new Pencil(),new Crayon() };
        for (Device device: devices) {
            for(Shape shape: shapes) {
                for(Brush brush: brushes) {
                    System.out.println(UsingMultipleDispatch.invoke(device,"draw", shape, brush));
                }
            }
        }
        // System.out.println( "Hello World!" );
    }
}
