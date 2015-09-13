package de.windowsfreak.testjni;

import de.windowsfreak.testjni.codec.AbstractCodec;
import de.windowsfreak.testjni.codec.WindowCodec;
import de.windowsfreak.testjni.writer.TCPWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Main5 {
    final String name;
    final Config config;
    final ImageReaderFactory sif;
    AbstractCodec pc;
    final TCPWriter writer;
    WindowCodec wc;

    long u = System.currentTimeMillis();
    int i = 0, compressedLength;
    long c = 0, z = 0;

    final ArrayList<CompressionWorker> workers = new ArrayList<CompressionWorker>();
    int nextIn = 0;
    int nextOut = 0;
    int threads;

    public Main5(final Config config) throws IOException {
        this(config, "Main5");
    }
    public Main5(final Config config, final String name) throws IOException {
        Thread.currentThread().setName(name + " Main5");
        this.name = name;
        this.config = config;
        this.threads = config.threads;

        if (config.sinkControlled) {
            writer = new TCPWriter();
            writer.initialize();
            ByteBuffer header = null;
            while (header == null) {
                header = writer.receiveCommandHeader();
                if (header == null) {
                    System.out.println("Waiting for header!");
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Header received.");
            if (header.get() == 1) {
                config.depth = header.get();
                config.fps = header.get();
                config.mode = header.get();
                config.x = header.getShort();
                config.y = header.getShort();
            }
        } else if (config.sink.equals("tcp")) {
            writer = new TCPWriter();
            writer.initialize();
        } else {
            writer = null;
        }
        sif = ImageReaderFactory.getFactory(config.source);

        if (sif == null) return;
        sif.start(config);

        if (!config.sinkControlled) {
            config.x = sif.getX();
            config.y = sif.getY();
        }

        if (config.compression != null) {
            CodecFactory codecFactory = CodecFactory.getFactory(config.compression);
            if (codecFactory != null)
            {
                pc = codecFactory.getCodec(config.x, config.y);
            } else {
                pc = null;
            }
        } else {
            pc = null;
        }

        for (int i = 0; i < threads; i++) {
            workers.add(new CompressionWorker());
            workers.get(i).setName(name + " Worker " + i);
            workers.get(i).start();
            System.out.println(name + " Activating worker " + i);
        }

        final Thread schedulerThread = new Thread() {
            public void run() {
                try {
                    System.out.println(getName() + " Starting schedule");
                    while (!interrupted()) {
                        schedule();
                    }
                } catch (InterruptedException e) {
                    System.out.println(getName() + " Scheduler stopped!"); //e.printStackTrace();
                }
            }
        };
        Thread deliveryThread = new Thread() {
            public void run() {
                if (config.sink.equals("window")) {
                    wc = new WindowCodec();
                    wc.setDimensions(config.x, config.y);
                    try {
                        wc.encode(null, null, 0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    wc.setTitle("Image viewer");
                    try {
                        while (wc.window.isVisible()) {
                            System.out.println(getName() + " Starting delivery");
                            deliver();
                        }
                        sif.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else if (config.sink.equals("tcp")) {
                    if (writer == null) {
                        System.out.println("Writer not initialized :-(");
                        return;
                    }

                    try {
                        while (writer.instances.size() < 1) {
                            System.out.println("Waiting for first client!");
                            Thread.sleep(500);
                        }
                        while (writer.instances.size() > 0) {
                            System.out.println(getName() + " Starting delivery");
                            deliver();
                        }
                        System.out.println("No more clients connected!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    sif.stop();
                    try {
                        writer.shutDown();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                schedulerThread.interrupt();
                for (CompressionWorker worker : workers) {
                    worker.interrupt();
                }
                System.out.println(getName() + " Delivery stopped!");
            }
        };
        schedulerThread.setName(name + " scheduler");
        schedulerThread.start();
        deliveryThread.setName(name + " delivery");
        deliveryThread.start();
    }

    public void schedule() throws InterruptedException {
        CompressionWorker worker = workers.get(nextIn);
        System.out.println(name + " Schedule: Waiting for worker " + nextIn);
        synchronized(worker.resetLock) {
            while (!worker.canReset) {
                System.out.println(name + " Schedule: Worker not ready " + nextIn);
                worker.resetLock.wait();
            }
        }
        System.out.println(name + " Schedule: Start worker " + nextIn);
        synchronized(worker.startLock) {
            worker.canStart = true;
            worker.startLock.notify();
        }
        synchronized(worker.obtainLock) {
            while (!worker.obtained) {
                worker.obtainLock.wait();
            }
            worker.obtained = false;
            System.out.println(name + " Schedule: Leaving worker " + nextIn);
        }
        nextIn++;
        if (nextIn >= threads)
            nextIn = 0;
    }

    public void deliver() throws InterruptedException {
        CompressionWorker worker = workers.get(nextOut);
        System.out.println(name + " Deliver: Waiting for worker " + nextOut);
        synchronized(worker.completeLock) {
            while (!worker.complete) {
                System.out.println(name + " Deliver: Worker still busy " + nextOut);
                worker.completeLock.wait();
            }
        }
        System.out.println(name + " Deliver: Delivering " + nextOut);
        worker.deliver();
        System.out.println(name + " Deliver: Reseting " + nextOut);
        worker.reset();
        nextOut++;
        if (nextOut >= threads)
            nextOut = 0;
    }

    class CompressionWorker extends Thread {
        final ByteBuffer sourceBuffer = ByteBuffer.allocateDirect((int)config.x * (int)config.y << 1);
        final ByteBuffer compressedBuffer;
        boolean canStart = false;
        boolean obtained = false;
        boolean complete = false;
        boolean canReset = true;
        public final Object startLock = new Object();
        public final Object obtainLock = new Object();
        public final Object completeLock = new Object();
        public final Object resetLock = new Object();
        public CompressionWorker() {
            super();
            if (pc != null) {
                compressedBuffer = ByteBuffer.allocateDirect((int)config.x * (int)config.y << 1);
            } else {
                compressedBuffer = null;
            }
        }
        public void run() {
            try {
                while (true) {
                    synchronized(startLock) {
                        while (!canStart) {
                            System.out.println(getName() + ": Waiting for Start");
                            startLock.wait();
                        }
                        System.out.println(getName() + ": Start");
                        canStart = false;
                        canReset = false;
                        complete = false;
                    }
                    try {
                        System.out.println(getName() + ": Work");
                        work();
                        System.out.println(getName() + ": Done");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(getName() + ": stopped!"); //e.printStackTrace();
            }
        }
        void reset() {
            System.out.println(getName() + ": Reset");
            complete = false;
            synchronized(resetLock) {
                canReset = true;
                resetLock.notify();
            }
        }
        void work() throws IOException {
            // obtain
            if (pc != null && config.decompress && !config.compress) {
                sif.factor(compressedBuffer);
            } else {
                sif.factor(sourceBuffer);
            }
            System.out.println(getName() + ": Obtained");
            synchronized(obtainLock) {
                obtained = true;
                obtainLock.notify();
            }

            System.out.println(getName() + ": Encoding");
            // encode
            if (pc != null) {
                if (config.compress) {
                    pc.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                }
                if (config.decompress) {
                    pc.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());
                }
            }

            // provide
            System.out.println(getName() + ": Complete");
            synchronized(completeLock) {
                complete = true;
                completeLock.notify();
            }
        }

        void deliver() {
            if (config.sink.equals("window")) {
                if (pc != null && config.compress && !config.decompress) {
                    wc.window.setSourceImage(compressedBuffer, config.x, config.y);
                } else {
                    wc.window.setSourceImage(sourceBuffer, config.x, config.y);
                }
                if (compressedBuffer != null) c += compressedBuffer.limit();
                z += sourceBuffer.limit();
                long v = System.currentTimeMillis();
                i++;
                if (i == 10) {
                    wc.window.setTitle("Video stream: " + (i * 1000 / (v - u)) + "fps, " + (c / i) + " bytes (" + (c * 100 / z) + "%), " + ((i * 1000 / (v - u)) * (c / i)) + "bps");
                    u = v;
                    i = 0;
                    c = 0;
                    z = 0;
                }
            } else if (config.sink.equals("tcp")) {
                if (config.compress && !config.decompress) {
                    writer.writeImage(config.x, config.y, compressedBuffer);
                } else {
                    writer.writeImage(config.x, config.y, sourceBuffer);
                }
            }
        }
    }
}

