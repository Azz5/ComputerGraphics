public class Main {

    static Window window = new Window(1280,720);
    public static void main(String[] args) {
            window.create("Block World");
            Engine engine = new Engine();
            engine.run();
            window.destroy();
    }
}