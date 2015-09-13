package org.openni.android.tools.niviewer;

import java.util.ArrayList;
import java.util.Arrays;

public class Installation extends ExecuteAsRootBase {

	@Override
	protected ArrayList<String> getCommandsToExecute() {
		// TODO Auto-generated method stub
		// https://github.com/libusb/libusb/tree/master/android
		return new ArrayList<String>(Arrays.asList(new String[] {"chmod -R 777 /dev/usb/*", "chmod -R 777 /dev/bus/usb/*"}));
	}

}
