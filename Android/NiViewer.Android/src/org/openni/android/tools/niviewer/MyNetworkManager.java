package org.openni.android.tools.niviewer;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;
import org.openni.VideoFrameRef;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by lazer_000 on 06.05.2015.
 */
public class MyNetworkManager extends Thread {
    private Activity activity;
    private ServerSocket server;
    public VideoFrameRef frame;
    public Object waitLock = new Object();

    public static MyNetworkManager instance;

    public MyNetworkManager(Activity activity) {
        instance = this;
        this.activity = activity;
    }
    public void startup() {
        shutdown();
        try {
            server = new ServerSocket(4321);
            Log.d("NETWORK", "Now listening on port 4321.");
        } catch (IOException e) {
            Log.d("NETWORK", "Could not listen on port 4321: " + e.getMessage());
        }

        WifiManager wm = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());

        Log.d("NETWORK", "IP address: " + ip);
        MyToaster.toast("IP address: " + ip);
        start();
    }
    public void shutdown() {
        if (server != null && !server.isClosed()) {
            try {
                server.close();
                Log.d("NETWORK", "Server has been shut down.");
            } catch (IOException e) {
                Log.d("NETWORK", "Could not shut down server cleanly: " + e.getMessage());
            }
        }
    }
    public void run() {
        while ((server != null && !server.isClosed())) {
            Socket client;

            try{
                client = server.accept();
                Log.d("NETWORK", "New Client accepted.");
            } catch (IOException e) {
                Log.d("NETWORK", "Accepting a new client failed:" + e.getMessage());
                continue;
            }

            MyClientManager cm = new MyClientManager(this, client);
        }
    }

    public void updateFrame(VideoFrameRef frame) {
        synchronized(this.waitLock) {
            this.frame = frame;
            if (frame != null)
                this.waitLock.notifyAll();
        }
    }
}
