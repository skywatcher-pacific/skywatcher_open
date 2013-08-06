package com.skywatcher.PanoramaApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

// It is a helper class to manage the bluetooth connection
// it will moniter the bluetooth connection condition
// and provide connect, reconnect interface of Mount
public class BluetoothManager {
	// Debugging
	private static final String TAG = "BluetoothManager";
	private static final boolean D = true;

	// Name for the SDP record when creating server socket
	private static final String NAME = "BluetoothChat";

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	// public static final int MESSAGE_REQUEST_CONNECT = 6;
	// public static final int MESSAGE_REQUEST_RECONNECT = 7;

	public static final int MESSAGE_CONNECTED = 8;
	public static final int MESSAGE_CONNECTION_LOST = 9;
	public static final int MESSAGE_RECONNECTED = 10;
	public static final int MESSAGE_CONNECTION_FAILED = 11;
	public static final int MESSAGE_CONNECTION_STOP = 12;

	// The event that bluetooth service will send
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Unique UUID for this application
	// private static final UUID MY_UUID =
	// UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	private static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Member fields
	private static BluetoothAdapter mAdapter;
	private static Handler mHandler;
	// private static AcceptThread mAcceptThread;
	private static ConnectThread mConnectThread;
	// private static ConnectedThread mConnectedThread;
	private static int mState;
	private static BluetoothSocket mSocket;
	private static String mAddress;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
//	public static final int STATE_LISTEN = 1; // now listening for incoming
	// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
	// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
	// connection lost
	public static final int STATE_CONNECTION_LOST = -1; // now lost connection
	// Reconnect
	public static final int STATE_RECONNECTING = 4;
	public static final int STATE_RECONNECTED = 5;

	// to a remote device

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothManager(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (D)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState() {
		return mState;
	}

	// public synchronized void requestConnection(String address) {
	// mHandler.obtainMessage(MESSAGE_REQUEST_CONNECT, address).sendToTarget();
	// }
	//
	// public synchronized void requestReconnect() {
	// mHandler.obtainMessage(MESSAGE_REQUEST_RECONNECT, mAddress)
	// .sendToTarget();
	// }

	public synchronized BluetoothSocket getSocket() {
		return mSocket;
	}

	// /**
	// * Start the chat service. Specifically start AcceptThread to begin a
	// * session in listening (server) mode. Called by the Activity onResume()
	// */
	// public synchronized void start() {
	// if (D)
	// Log.d(TAG, "start");
	//
	// // Cancel any thread attempting to make a connection
	// if (mConnectThread != null) {
	// mConnectThread.cancel();
	// mConnectThread = null;
	// }
	//
	// // // Cancel any thread currently running a connection
	// // if (mConnectedThread != null) {
	// // mConnectedThread.cancel();
	// // mConnectedThread = null;
	// // }
	// //
	// // // Start the thread to listen on a BluetoothServerSocket
	// // if (mAcceptThread == null) {
	// // mAcceptThread = new AcceptThread();
	// // mAcceptThread.start();
	// // }
	// setState(STATE_LISTEN);
	// }

	public synchronized void connect(String address) {
		if (D)
			Log.i(TAG, "called connect! " + address);

		// Init Bluetooth Connection
		BluetoothDevice device = mAdapter.getRemoteDevice(address);
		connect(device);
		mAddress = address;
	}

	public synchronized void reconnect() {
		if (D)
			Log.i(TAG, "called reconnect!");

		// Init Bluetooth Connection
		BluetoothDevice device = mAdapter.getRemoteDevice(mAddress);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// // Cancel any thread currently running a connection
		// if (mConnectedThread != null) {
		// mConnectedThread.cancel();
		// mConnectedThread = null;
		// }

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_RECONNECTING);
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device) {
		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// // Cancel any thread currently running a connection
		// if (mConnectedThread != null) {
		// mConnectedThread.cancel();
		// mConnectedThread = null;
		// }

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/*
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket The BluetoothSocket on which the connection was made
	 * 
	 * @param device The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// // Cancel any thread currently running a connection
		// if (mConnectedThread != null) {
		// mConnectedThread.cancel();
		// mConnectedThread = null;
		// }
		//
		// // Cancel the accept thread because we only want to connect to one
		// // device
		// if (mAcceptThread != null) {
		// mAcceptThread.cancel();
		// mAcceptThread = null;
		// }
		//
		// // Start the thread to manage the connection and perform
		// transmissions
		// mConnectedThread = new ConnectedThread(socket);
		// mConnectedThread.start();
		mSocket = socket;

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		if (mState == STATE_RECONNECTING) {
			setState(STATE_RECONNECTED);
			mHandler.obtainMessage(MESSAGE_RECONNECTED).sendToTarget();
		} else {
			setState(STATE_CONNECTED);
			mHandler.obtainMessage(MESSAGE_CONNECTED).sendToTarget();
		}
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "called stop");

		if (mConnectThread != null) {
			Thread Temp = mConnectThread;
			mConnectThread.cancel();
			mConnectThread = null;
			Temp.interrupt();
			try {
				Temp.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (mSocket != null)
			try {
				mSocket.close();
				Log.i(TAG, "close mSocket success");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "close mSocket Failed", e);
			}

		// if (mConnectedThread != null) {
		// Thread Temp = mConnectedThread;
		// mConnectedThread.cancel();
		// mConnectedThread = null;
		// Temp.interrupt();
		// try {
		// Temp.join();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		// if (mAcceptThread != null) {
		// Thread Temp = mAcceptThread;
		// mAcceptThread.cancel();
		// mAcceptThread = null;
		// Temp.interrupt();
		// try {
		// Temp.join();
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		setState(STATE_NONE);
		mHandler.obtainMessage(MESSAGE_CONNECTION_STOP).sendToTarget();
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	public synchronized void connectionFailed() {
		if(D)
			Log.i(TAG, "called connectionFailed");
		
		setState(STATE_NONE);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "Unable to connect device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		mHandler.obtainMessage(MESSAGE_CONNECTION_FAILED).sendToTarget();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	public synchronized void connectionLost() {
		if(D)
			Log.i(TAG, "called connectionLost");
		
		setState(STATE_NONE);

		// Send a connection lost event
		Message msg1 = mHandler.obtainMessage(MESSAGE_STATE_CHANGE,
				STATE_CONNECTION_LOST, -1);
		mHandler.sendMessage(msg1);

		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		mHandler.obtainMessage(MESSAGE_CONNECTION_LOST).sendToTarget();
	}

	// /**
	// * This thread runs while listening for incoming connections. It behaves
	// * like a server-side client. It runs until a connection is accepted (or
	// * until cancelled).
	// */
	// private class AcceptThread extends Thread {
	// // The local server socket
	// private final BluetoothServerSocket mmServerSocket;
	//
	// public AcceptThread() {
	// BluetoothServerSocket tmp = null;
	//
	// // Create a new listening server socket
	// try {
	// tmp = mAdapter
	// .listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
	// } catch (IOException e) {
	// Log.e(TAG, "listen() failed", e);
	// }
	// mmServerSocket = tmp;
	// }
	//
	// public void run() {
	// if (D)
	// Log.d(TAG, "BEGIN mAcceptThread" + this);
	// setName("AcceptThread");
	// BluetoothSocket socket = null;
	//
	// // Listen to the server socket if we're not connected
	// while (mState != STATE_CONNECTED) {
	// if (this.interrupted())
	// return;
	//
	// try {
	// // This is a blocking call and will only return on a
	// // successful connection or an exception
	// socket = mmServerSocket.accept();
	// } catch (IOException e) {
	// Log.e(TAG, "accept() failed", e);
	// break;
	// }
	//
	// // If a connection was accepted
	// if (socket != null) {
	// synchronized (BluetoothManager.this) {
	// switch (mState) {
	// case STATE_LISTEN:
	// case STATE_CONNECTING:
	// // Situation normal. Start the connected thread.
	// connected(socket, socket.getRemoteDevice());
	// break;
	// case STATE_NONE:
	// case STATE_CONNECTED:
	// // Either not ready or already connected.
	// // Terminate new socket.
	// try {
	// socket.close();
	// } catch (IOException e) {
	// Log
	// .e(
	// TAG,
	// "Could not close unwanted socket",
	// e);
	// }
	// break;
	// }
	// }
	// }
	// }
	// if (D)
	// Log.i(TAG, "END mAcceptThread");
	// }
	//
	// public void cancel() {
	// if (D)
	// Log.d(TAG, "cancel " + this);
	// try {
	// mmServerSocket.close();
	// } catch (IOException e) {
	// Log.e(TAG, "close() of server failed", e);
	// }
	// }
	// }

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;

		public ConnectThread(BluetoothDevice device) {
			mmDevice = device;
			BluetoothSocket tmp = null;

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try {
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
				Log.e(TAG, "create() failed", e);
			}

			Log.i(TAG, "create() successed");
			mmSocket = tmp;
		}

		public void run() {
			Log.i(TAG, "BEGIN mConnectThread");
			setName("ConnectThread");

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				Log.d(TAG, "try to connect");
				mmSocket.connect();

				Log.i(TAG, "connect mmSocket success");

			} catch (IOException e) {
				Log.e(TAG, "connection failed! " + e.toString());
				connectionFailed();
				// Close the socket
				try {
					mmSocket.close();
					Log.i(TAG, "close mmSocket success");
				} catch (IOException e2) {
					Log
							.e(
									TAG,
									"unable to close() socket during connection failure",
									e2);
				}
				// Start the service over to restart listening mode
				// BluetoothManager.this.start();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothManager.this) {
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice);
		}

		public void cancel() {
			try {
				mmSocket.close();
				Log.i(TAG, "close mmSocket success");
			} catch (IOException e) {
				Log.e(TAG, "close() of connect socket failed", e);
			}
		}
	}

	// /**
	// * This thread runs during a connection with a remote device. It handles
	// all
	// * incoming and outgoing transmissions.
	// */
	// private class ConnectedThread extends Thread {
	// private final BluetoothSocket mmSocket;
	// private final InputStream mmInStream;
	// private final OutputStream mmOutStream;
	// private final StringBuffer mmBuffer;
	//
	// public ConnectedThread(BluetoothSocket socket) {
	// Log.d(TAG, "create ConnectedThread");
	// mmSocket = socket;
	// InputStream tmpIn = null;
	// OutputStream tmpOut = null;
	//
	// // Get the BluetoothSocket input and output streams
	// try {
	// tmpIn = socket.getInputStream();
	// tmpOut = socket.getOutputStream();
	// } catch (IOException e) {
	// Log.e(TAG, "temp sockets not created", e);
	// }
	//
	// mmInStream = tmpIn;
	// mmOutStream = tmpOut;
	// mmBuffer = new StringBuffer();
	// }
	//
	// public void run() {
	// // Donot use the connected thread
	// return;
	// // Log.i(TAG, "BEGIN mConnectedThread");
	// // byte[] buffer = new byte[1024];
	// // int bytes;
	// //
	// // // Keep listening to the InputStream while connected
	// // // Not really useful, the delay of connection lost detect is
	// // about
	// // // 10 sec
	// // while (true) {
	// //
	// // if(this.interrupted())
	// // return;
	// // // try {
	// // // // Just check the connection is available in every half
	// // // // second
	// // // mmOutStream.write(0);
	// // // // int v = mmInStream.available();
	// // // Log.i(TAG, "mmOut, write still live");
	// // // } catch (IOException e) {
	// // // Log.e(TAG, "disconnected", e);
	// // // connectionLost();
	// // // break;
	// // // }
	// // try {
	// // mmInStream.available();
	// // Log.i(TAG, "mmIn, available still live");
	// // } catch (IOException e) {
	// // Log.e(TAG, "disconnected", e);
	// // connectionLost();
	// // return;
	// // }
	// // // try {
	// // // mmOutStream.flush();
	// // // Log.i(TAG, "mmOut, flush still live");
	// // // } catch (IOException e) {
	// // // Log.e(TAG, "disconnected", e);
	// // // connectionLost();
	// // // break;
	// // // }
	// // try {
	// // Thread.sleep(500);
	// // } catch (InterruptedException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // return;
	// // }
	// //
	// // }
	// }
	//
	// //
	// // /**
	// // * Write to the connected OutStream.
	// // *
	// // * @param buffer
	// // * The bytes to write
	// // */
	// // public void write(byte[] buffer) {
	// // try {
	// // mmOutStream.write(buffer);
	// //
	// // // Share the sent message back to the UI Activity
	// // mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
	// // .sendToTarget();
	// // } catch (IOException e) {
	// // Log.e(TAG, "Exception during write", e);
	// // }
	// // }
	// //
	// // /**
	// // * Write to the connected OutStream. synced
	// // *
	// // * @param buffer
	// // * The bytes to write
	// // * @throws IOException
	// // * @throws IOException
	// // */
	// // public String conversation(byte[] buffer) throws IOException {
	// // mmBuffer.setLength(0);
	// // write(buffer);
	// //
	// // // try {
	// // // Thread.currentThread().sleep(500);
	// // // } catch (InterruptedException e1) {
	// // // // TODO Auto-generated catch block
	// // // e1.printStackTrace();
	// // // }
	// // long mLastCommandSendTime = System.currentTimeMillis();
	// //
	// // do {
	// // // if(mmBuffer.length() > 0)
	// // // Log.d(TAG, "Read From Buffer :" + mmBuffer.toString());
	// // int indexOfStartSymbol2 = mmBuffer.lastIndexOf("=");
	// // int indexOfStartSymbol1 = mmBuffer.lastIndexOf("!");
	// // int indexOfStartSymbol = indexOfStartSymbol1 >= 0 ?
	// // indexOfStartSymbol1
	// // : indexOfStartSymbol2;
	// //
	// // int indexOfEndSymbol = mmBuffer.lastIndexOf("" + '\r');
	// // if (indexOfStartSymbol >= 0 && indexOfEndSymbol >= 0
	// // && indexOfEndSymbol > indexOfStartSymbol)
	// // // mStream.lastIndexOf(0x13.toString()))
	// // {
	// // String result;
	// // synchronized (this) {
	// // result = mmBuffer.toString().substring(
	// // indexOfStartSymbol, indexOfEndSymbol);
	// // mmBuffer.setLength(0); // Clean the Buffer;
	// // }
	// // return result;
	// // }
	// //
	// // // try {
	// // // Thread.currentThread().sleep(100);
	// // // } catch (InterruptedException e) {
	// // // // TODO Auto-generated catch block
	// // // e.printStackTrace();
	// // // }
	// // } while (System.currentTimeMillis() - mLastCommandSendTime < 1000);
	// // Log.e(TAG, "conversation time out");
	// // throw new IOException();
	// // }
	//
	// public void cancel() {
	// try {
	// mmSocket.close();
	// } catch (IOException e) {
	// Log.e(TAG, "close() of connect socket failed", e);
	// }
	// }
	// }
}
