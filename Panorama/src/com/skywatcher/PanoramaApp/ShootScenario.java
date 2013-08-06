package com.skywatcher.PanoramaApp;

import android.os.Parcel;
import android.os.Parcelable;

public class ShootScenario implements Parcelable {
	private long rowid;
	private String name;
	private int minAz;
	private int maxAz;
	private int minAlt;
	private int maxAlt;
	private boolean interlace;

	public long getRowid() {
		return rowid;
	}

	public void setRowid(long rowid) {
		this.rowid = rowid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMinAz() {
		return minAz;
	}

	public void setMinAz(int minAz) throws ShootScenarioException {
		if (minAz < -180 || minAz > 180)
			throw new ShootScenarioException("MinAz is out of range.");
		this.minAz = minAz;
	}

	public int getMaxAz() {
		return maxAz;
	}

	public void setMaxAz(int maxAz) throws ShootScenarioException {
		if (maxAz < -180 || maxAz > 180)
			throw new ShootScenarioException("MaxAz is out of range.");
		this.maxAz = maxAz;
	}

	public int getMinAlt() {
		return minAlt;
	}

	public void setMinAlt(int minAlt) throws ShootScenarioException {
		if (minAlt < -50 || minAlt > 90)
			throw new ShootScenarioException("MinAlt is out of range.");
		this.minAlt = minAlt;
	}

	public int getMaxAlt() {
		return maxAlt;
	}

	public void setMaxAlt(int maxAlt) throws ShootScenarioException {
		if (maxAlt < -50 || maxAlt > 90)
			throw new ShootScenarioException("MaxAlt is out of range.");
		this.maxAlt = maxAlt;
	}

	public boolean isInterlace() {
		return interlace;
	}

	public void setInterlace(boolean interlace) {
		this.interlace = interlace;
	}

	public class ShootScenarioException extends Exception {

		private static final long serialVersionUID = 2L;

		public ShootScenarioException(String detailMessage) {
			super(detailMessage);
			// TODO Auto-generated constructor stub
		}

	}

	@Override
	public String toString() {
		return (name == null || name.trim().length() == 0) ? "Az:(" + minAz + "~" + maxAz + ") Alt:("
				+ minAlt + "~" + maxAlt + ")" : name;		
	}

	// 以下是為了實作 Parcelable 用的
	// 將資料序列化

	public static final Parcelable.Creator<ShootScenario> CREATOR = new Parcelable.Creator<ShootScenario>() {
		public ShootScenario createFromParcel(Parcel in) {
			return new ShootScenario(in);
		}

		public ShootScenario[] newArray(int size) {
			return new ShootScenario[size];
		}
	};

	public ShootScenario() {
	}

	public ShootScenario(Parcel in) {
		readFromParcel(in);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(rowid);
		out.writeString(name);
		out.writeInt(minAz);
		out.writeInt(maxAz);
		out.writeInt(minAlt);
		out.writeInt(maxAlt);
		out.writeValue(new Boolean(interlace));
	}

	public void readFromParcel(Parcel in) {
		rowid = in.readLong();
		name = in.readString();
		minAz = in.readInt();
		maxAz = in.readInt();
		minAlt = in.readInt();
		maxAlt = in.readInt();
		interlace = ((Boolean) in.readValue(null)).booleanValue();
	}
}
