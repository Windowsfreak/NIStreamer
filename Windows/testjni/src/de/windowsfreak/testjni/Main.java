package de.windowsfreak.testjni;

import de.windowsfreak.testjni.codec.AbstractCodec;
import de.windowsfreak.testjni.writer.Sink;
import de.windowsfreak.testjni.writer.WindowSink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class Main {
    final String name;
    final Config config;
    final SourceFactory sif;
    AbstractCodec codec;
    final Sink sink;
    int[] frameIds;

    long ubase = System.currentTimeMillis();
    int ucount = 0;
    long ubytes = 0, ucomp = 0;

    final ArrayList<CompressionWorker> workers = new ArrayList<CompressionWorker>();
    int nextIn = 0;
    int nextOut = 0;
    int threads;
    final Thread schedulerThread;
    final Thread deliveryThread;

    public Main(final Config config) throws IOException {
        this(config, "Main");
    }
    public Main(final Config config, final String name) throws IOException {
        Thread.currentThread().setName(name + " Main");
        this.name = name;
        this.config = config;
        this.threads = config.threads;
        frameIds = new int[threads];

        SinkFactory sf = SinkFactory.getFactory(config.sink);
        sink = sf.getSink(config);
        sif = SourceFactory.getFactory(config.source);

        if (sif == null) throw new IOException("sif not found!");
        sif.start(config);

        if (config.compression != null) {
            CodecFactory codecFactory = CodecFactory.getFactory(config.compression);
            if (codecFactory != null)
            {
                codec = codecFactory.getCodec(config);
            } else {
                codec = null;
            }
        } else {
            codec = null;
        }

        for (int i = 0; i < threads; i++) {
            workers.add(new CompressionWorker(i));
            workers.get(i).setName(name + " Worker " + i);
            workers.get(i).start();
            if (config.debug) System.out.println(name + " Activating worker " + i);
        }

        schedulerThread = new Thread() {
            public void run() {
                try {
                    System.out.println(getName() + " Starting schedule");
                    while (!interrupted() && config.condition) {
                        schedule();
                    }
                } catch (InterruptedException e) {
                    System.out.println(getName() + " Scheduler stopped!"); //e.printStackTrace();
                }
            }
        };
        deliveryThread = new Thread() {
            public void run() {
                if (sink == null) {
                    System.out.println("Sink not initialized :-(");
                    return;
                }

                try {
                    while (sink.condition(config) && config.condition) {
                        if (config.debug) System.out.println(getName() + " Starting delivery");
                        deliver();
                    }
                    System.out.println("No more clients connected!");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sif.stop(config);
                try {
                    sink.shutDown();
                } catch (IOException e) {
                    e.printStackTrace();
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
        if (config.debug) System.out.println(name + " Schedule: Waiting for worker " + nextIn);
        synchronized(worker.resetLock) {
            while (!worker.canReset && config.condition) {
                if (config.debug) System.out.println(name + " Schedule: Worker not ready " + nextIn);
                worker.resetLock.wait();
            }
        }
        if (config.debug) System.out.println(name + " Schedule: Start worker " + nextIn);
        synchronized(worker.startLock) {
            worker.canStart = true;
            worker.startLock.notify();
        }
        synchronized(worker.obtainLock) {
            while (!worker.obtained && config.condition) {
                worker.obtainLock.wait();
            }
            worker.obtained = false;
            if (config.debug) System.out.println(name + " Schedule: Leaving worker " + nextIn);
        }
        nextIn++;
        if (nextIn >= threads)
            nextIn = 0;
    }

    public void deliver() throws InterruptedException {
        CompressionWorker worker = workers.get(nextOut);
        if (config.debug) System.out.println(name + " Deliver: Waiting for worker " + nextOut);
        synchronized(worker.completeLock) {
            while (!worker.complete && config.condition) {
                if (config.debug) System.out.println(name + " Deliver: Worker still busy " + nextOut);
                worker.completeLock.wait();
            }
        }
        if (config.debug) System.out.println(name + " Deliver: Delivering " + nextOut);
        worker.deliver();
        if (config.debug) System.out.println(name + " Deliver: Reseting " + nextOut);
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
        public final int threadId;
        public CompressionWorker(int threadId) {
            super();
            this.threadId = threadId;
            if (codec != null) {
                compressedBuffer = ByteBuffer.allocateDirect((int)config.x * (int)config.y << 1);
            } else {
                compressedBuffer = null;
            }
        }
        public void run() {
            try {
                while (config.condition) {
                    synchronized(startLock) {
                        while (!canStart && config.condition) {
                            if (config.debug) System.out.println(getName() + ": Waiting for Start");
                            startLock.wait();
                        }
                        if (config.debug) System.out.println(getName() + ": Start");
                        canStart = false;
                        canReset = false;
                        complete = false;
                    }
                    try {
                        if (config.debug) System.out.println(getName() + ": Work");
                        work();
                        if (config.debug) System.out.println(getName() + ": Done");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                System.out.println(getName() + ": stopped!"); //e.printStackTrace();
            }
        }
        void reset() {
            if (config.debug) System.out.println(getName() + ": Reset");
            complete = false;
            synchronized(resetLock) {
                canReset = true;
                resetLock.notify();
            }
        }
        void work() throws IOException {
            // obtain
            if (codec != null && config.decompress && !config.compress) {
                sif.factor(config, compressedBuffer);
            } else {
                sif.factor(config, sourceBuffer);
            }
            frameIds[threadId] = config.frameId;
            if (config.debug) System.out.println(getName() + ": Obtained");
            synchronized(obtainLock) {
                obtained = true;
                obtainLock.notify();
            }

            if (config.debug) System.out.println(getName() + ": Encoding");
            // encode
            if (codec != null) {
                if (config.benchmark) {
                    System.out.println("Starting benchmark for Thread " + Thread.currentThread().getName());
                    long t = System.currentTimeMillis();
                    for (int x = 0; x < 100; x++) {
                        if (config.compress) {
                            codec.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                        }
                        if (config.decompress) {
                            codec.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());
                        }
                    }
                    t = System.currentTimeMillis() - t;
                    System.out.println("Stopping benchmark for Thread " + Thread.currentThread().getName() + ": " + t + "ms for 100 frames.");
                } else {
                    if (config.compress) {
                        codec.encode(compressedBuffer, sourceBuffer, sourceBuffer.capacity());
                    }
                    if (config.decompress) {
                        codec.decode(sourceBuffer, sourceBuffer.capacity(), compressedBuffer, compressedBuffer.remaining());
                    }
                }
            }

            // provide
            if (config.debug) System.out.println(getName() + ": Complete");
            synchronized(completeLock) {
                complete = true;
                completeLock.notify();
            }
        }

        void deliver() {
                if (config.compress && !config.decompress) {
                    sink.writeImage(config, compressedBuffer, frameIds[threadId]);
                } else {
                    sink.writeImage(config, sourceBuffer, frameIds[threadId]);
                }
            ubytes += sourceBuffer.capacity();
            ucomp += (compressedBuffer != null) ? compressedBuffer.limit() : sourceBuffer.capacity();
            ucount++;
            final long unew = System.currentTimeMillis();
            if ((ucount & 15) == 0 && unew - ubase > 5000) {
                int fcd = config.frameId - frameIds[threadId];
                float fps = (ucount * 100000 / (unew - ubase)) / 100f;
                float ratio = (ucomp * 10000 / ubytes) / 100f;
                long bandwidth = (ucomp / (unew - ubase));
                System.out.println(
                        name + "Performance measurement:\n" +
                                "| Frame Deviation: " + fcd + " frames\n" +
                                "| Frame per sec  : " + fps + " fps\n" +
                                "| CompressionRate: " + ratio + " %\n" +
                                "| Bandwidth      : " + bandwidth + " thousand bytes / s\n"
                );
                if (sink instanceof WindowSink && ((WindowSink) sink).window != null)
                    ((WindowSink) sink).window.setTitle(name + ": FCD=" + fcd + ", FPS=" + fps + ", Ratio=" + ratio + "%, Bandwidth = " + bandwidth + "k/s");
                ubase = unew;
                ucomp = ubytes = ucount = 0;
            }
        }
    }
}

