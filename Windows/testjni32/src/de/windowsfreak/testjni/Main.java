package de.windowsfreak.testjni;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        //Main1.main(args);
        //Main2.main(args);
        //Main3.main(args);
        final Config config = new Config();
        config.source = "oni";
        config.sink = "window";
        config.compression = "CharLS";
        config.compress = true;
        config.decompress = true;
        config.threads = 1;
        config.x = 640;
        config.y = 480;
        new Main5(config, "De");
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
                    new Main5(config1, "En");
                    //Main4.main(config1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        new Main5(config2, "De");
        //Main4.main(config2);
        */
    }
}

