package com.skywatcher.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class SerialConnection_Bluetooth extends SerialConnection {
	// Debugging
	private static final String TAG = "BluetoothManager";
	private static final boolean D = true;

	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;
	private final byte[] buffer = new byte[1024];

	public SerialConnection_Bluetooth(BluetoothSocket socket)
			throws IOException {
		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;
		// BufferedWriter writer;

		// Get the BluetoothSocket input and output streams
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
			Log.i(TAG, "Get I/O Stream Success");
		} catch (IOException e) {
			Log.e(TAG, "temp sockets not created", e);
			throw e;
		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	@Override
	public void Write(String Command) throws IOException {
		// TODO Auto-generated method stub
		if (D)
			Log.i(TAG, "Called Serial Connection Write");

		mmOutStream.write(Command.getBytes(Encoding));
		this.StringSent = Command;

		if (D)
			Log.i(TAG, "Called Serial Connection Write Success");

	}

	@Override
	public String Read() throws IOException {
		// TODO Auto-generated method stub

		if (D)
			Log.i(TAG, "Called Serial Connection Read");
		
		int bytes = mmInStream.read(buffer);
		String ReadBuffer = new String(buffer, 0, bytes, Encoding);

		if (D)
			Log.i(TAG, "Called Serial Connection Read Success");

		return ReadBuffer;
	}

	@Override
	public void ClearBuffer() {
		mBuffer.setLength(0);
		// TODO Auto-generated method stub
	}

//	@Override
//	public void Close() throws IOException {
//		if (D)
//			Log.i(TAG, "Called Serial Connection Close");
//
//		// TODO Auto-generated method stub
//		mmSocket.close();
//
//		Log.i(TAG, "Serial Connection Connection Close Success");
//	}

}
