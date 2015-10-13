package room107.tool.douban;

/**
 * @author WangXiao
 */
public class CmdUtils {

    static void exec(final String cmd, int timeout) throws Exception {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Runtime run = Runtime.getRuntime();
                try {
                    Process process = run.exec("cmd.exe /K start " + cmd);
                    process.waitFor();
                    process.destroy();
                } catch (InterruptedException e) {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        t.start();
        Thread.sleep(timeout);
        if (t.isAlive()) {
            t.interrupt();
        }
    }

}
