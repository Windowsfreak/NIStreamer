This repository intentionally does not have any `.gitignore` files in order to include all project files, immediate files and binary files.
Get started by rooting your Android device and copying the APK of the NIStreamer (`/Android/NIViewer`). Using a development environment is recommended to launch testjni (`/Windows/testjni`).
# Table of Contents
- [/Android](#)
	- [/Android/NIViewer](#)
		- [About NIStreamer](#)
		- [Important Notes](#)
		- [APK file](#)
	- [/Android/NiViewer.Android](#)
		- [About NiViewer](#)
		- [Important Notes](#)
		- [APK file](#)
	- [/Android/OpenNI2-develop](#)
- [/Windows](#)
	- [/Windows/testjni](#)
		- [About testjni](#)
		- [Important Notes](#)
		- [JAR file](#)
	- [/Windows/CharLS](#)
		- [About CharLS](#)
		- [Important Notes](#)
	- [/Windows/libs](#)
		- [BZip2](#)
		- [CharLS](#)
		- [hadoop](#)
		- [ImageIO](#)
		- [OpenNI2](#)
		- [uiDesigner](#)
	- [/Windows/TCPClient](#)
		- [About TCPClient](#)
- [Contact info](#)

# `/Android`
## `/Android/NIViewer`
### About NIStreamer
NiStreamer is the Android port of testjni.
### Important Notes
The project __NiStreamer__ is saved in the folder NiViewer. It can be compiled using __Android Studio 1.3 RC3__, __JDK 1.7.0_79__ and the __Android API 22 Platform__. The NDK header version is 1.6, but 1.4 and 1.6 is __both supported__, unlike NiViewer.Android, where only NDK 1.4 is supported.

The OpenNI2 libraries are contained and work on any `armeabi-v7a` compatible architecture. They are automatically bundled with the APK file and loaded when the first OpenNI2 device is used during execution. It will then also use root to fix permissions on the USB devices and automatically load the necessary device. All compression codecs are integrated into the source tree and compiled during the build process. The Hadoop package and the snappy codec is stripped by a couple of classes to ensure compatibility with the Android SDK.
### APK file
A compiled APK file can be found in `/Android/NiViewer/app/build/outputs/apk`.
## `/Android/NiViewer.Android`
### About NiViewer
__NIViewer__ is an application that provides all the streams of an attached sensor and lets the user inspect the raw, unprocessed depth images or different types of color maps. The user may choose between different streams and add or remove them as desired.

Additionally, sessions can be recorded in ONI files, resolutions and sensor types can be changed and distance can be measured. It is already an inherent part of the OpenNI2 framework and intended for testing purposes, shows depth images of a connected sensor and allows for simple measurements.
### Important Notes
This is the Android port of the __NiViewer__. It uses root to fix permissions on the USB devices and automatically loads the necessary device on startup. It __can not__ install the OpenNI2 drivers on the `/system/lib/` folder, although `System.LoadLibrary` requires them to be there. It has a built-in server that allows other clients to connect and receive uncompressed frames. The NiStreamer is __not__ compatible with the trasmission headers, and will not understand the incoming data. NiViewer is not multithreaded, but it supports viewing at multiple streams at the same time. The build script targets Android 4.4.4 and NDK Version 1.4, but this app should run on devices running Android 2.3 up to 5.1. Chances are, that the OpenNI2 libraries will not run on Android 5.0 or higher due to code changes in the clib in later Android versions.
### APK file
A compiled APK file can be found in `/Android/NiViewer.Android/out/production/NiViewer.Android`.
## `/Android/OpenNI2-develop`
Use this folder to recompile the OpenNI2 native libraries. Compiling has been tested on a Windows system, but it might also work on Linux. The initial code has been cloned from [Occipital's Github develop branch](https://github.com/occipital/OpenNI2/tree/develop) on July 25th 2015.

* The build target was changed to Android-22
* The supported JNI versions now include 1.4 and 1.6
* clib patches in various files
* patches to the compile scripts and interfaces.

For an overview of all changes, using a "diff" tool is recommended.
# `/Windows`
## `/Windows/testjni`
### About testjni
testjni is the Windows port of NIStreamer. An older development version using 32-bit Windows libraries is found in the folder `/Windows/testjni32`, and supports a few older compression codecs, but lacks a few important features such as performance measurements.
### Important Notes
The project __testjni__ is saved in the folder testjni. It can be compiled using __IntelliJ IDEA 14.1.4__, __JDK 1.8.0 (x64)__ and a few libraries listed below.

It contains two classes with a static Main method, the Loader and the Launcher. Loader uses a configuration file whereas Launcher opens a configuration window. The external libraries are to be found in the libs folder.
### JAR file
A compiled JAR file can be found in `/Windows/testjni/out/artifacts/testjni_jar`, but the Java executable requires many parameters to correctly load the JNI dependencies, so the use of a development environment to launch the application is preferred.
## `/Windows/CharLS`
### About CharLS
__CharLS__ is a library that compresses images using Jpeg-LS lossless compression format. It supports up to 16 bit per color channel and also grayscale images. In comparison to JPEG 2000 it is claimed to be about 3 times faster.
### Important Notes
This library uses a modified interface (`interface.cpp` and `interface.h`) that is used to connect to the Java Native Interface. It can be compiled using __CodeBlocks__ and __XMing_64__, while care must be taken due to bugs regarding the JNI support and different versions of XMing.
## `/Windows/libs`
### BZip2
This is the JBZip2 library.
### CharLS
This is the DLL file required to load CharLS on Windows x64 systems.
### hadoop
This is the hadoop common package.
### ImageIO
This is a hardly obtainable package of the Java ImageIO extensions.
### OpenNI2
This is a copy of the `redist` folder from the OpenNI2 Windows x64 release.
### uiDesigner
This is a library required to load the dialogs from the Launcher and WindowSink.
## `/Windows/TCPClient`
### About TCPClient
TCPClient is a tool that displays raw frame transmissions from the modified NIViewer. It connects to the IP address and port and counts the incoming packets. It is not compatible to the NiStreamer.
# Contact info
Any assistance required? [Ask me for help](http://windowsfreak.de/)!
