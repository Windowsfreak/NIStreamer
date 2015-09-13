package de.windowsfreak.testjni;

import java.io.IOException;

public class Loader {

    private static Thread lastRunningTask;

    public static void stop() {
        if (lastRunningTask != null) {
            lastRunningTask.interrupt();
        }
    }

    public static void main(String[] args) {
        //Main1.main(args);
        //Main2.main(args);
        //Main3.main(args);
        if (lastRunningTask != null) {
            lastRunningTask.interrupt();
            lastRunningTask = null;
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        lastRunningTask = new Thread() {
            Main main;

            public void run() {
                try {
                    //Main1.main(args);
                    //Main2.main(args);
                    //Main3.main(args);
                    final Config config = new Config();
                    //config.source = "test";
                    config.source = "test";
                    config.sourceUrl = "192.168.1.111";
                    //config.sourceUrl = "C:\\Users\\lazer_000\\SkyDrive\\Documents\\sample.oni";
                    config.sourcePort = 9999;
                    config.sink = "window";
                    config.sinkUrl = "";
                    config.sinkPort = 9999;
                    config.sinkControlled = false;
                    config.compression = /*"";*/"Deflate2";
                    config.compress = false;
                    config.decompress = false;
                    config.threads = 4;
                    config.x = 640;
                    config.y = 480;
                    config.fps = 30;
                    config.depth = 1;
                    config.mode = 2;
                    config.benchmark = false;
                    new Main(config, "NIClient");

                    //config.x = 640;
                    //config.y = 480;
                    //config.sourceUrl = "C:\\Software\\adt\\workspace\\testjni\\res\\DeltaE_16bit_gamma2.2.png";
//                    config.x = 800;
//                    config.y = 600;
                    //                 config.sourceUrl = "C:\\Software\\adt\\workspace\\testjni\\res\\Lena16_800x600.raw";
//                    config.x = 640;
//                    config.y = 480;
//                    config.sourceUrl = "C:\\Users\\lazer_000\\SkyDrive\\Documents\\sample.oni";


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            public void interrupt() {
                main.config.condition = false;
                main.schedulerThread.interrupt();
                main.deliveryThread.interrupt();
                for (Thread worker : main.workers) {
                    worker.interrupt();
                }
                main.sif.stop(main.config);
                if (main.sink != null) try {
                    main.sink.shutDown();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                super.interrupt();
            }
        };
        lastRunningTask.start();
        /*
        final Config config1 = new Config();
        config1.source = "oni";
        config1.sinkControlled = true;
        config1.sink = "tcp";
        config1.compression = "Deflate2";
        config1.compress = true;
        config1.threads = 4;
        final Config config2 = new Config();
        config2.source = "tcp";
        config2.sink = "window";
        config2.compression = "Deflate2";
        config2.decompress = true;
        config2.x = 640;
        config2.y = 480;
        config2.threads = 4;
        new Thread() {
            public void run() {
                try {
                    new Main(config1, "En");
                    //Main4.main(config1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Main(config2, "De");
        //Main4.main(config2);
        */
    }
}

