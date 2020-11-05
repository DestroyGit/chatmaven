public class Lesson4_Task1 {
    static Object mon = new Object();
    static volatile char symbol = 'A';
    static final int num = 5;

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                for (int i = 0; i < num; i++) {
                    synchronized (mon) {
                        while (symbol != 'A') {
                            mon.wait();
                        }
                        System.out.print(symbol);
                        symbol = 'B';
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < num; i++) {
                    synchronized (mon) {
                        while (symbol != 'B') {
                            mon.wait();
                        }
                        System.out.print(symbol);
                        symbol = 'C';
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                for (int i = 0; i < num; i++) {
                    synchronized (mon) {
                        while (symbol != 'C') {
                            mon.wait();
                        }
                        System.out.print(symbol);
                        symbol = 'A';
                        mon.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
