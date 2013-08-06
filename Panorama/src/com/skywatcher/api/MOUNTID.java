package com.skywatcher.api;

public enum MOUNTID {
	// Telescope ID, they must be started from 0 and coded continuously.
	ID_CELESTRON_AZ(0), // Celestron Alt/Az Mount
	ID_CELESTRON_EQ(1), // Celestron EQ Mount
	ID_SKYWATCHER_AZ(2), // Skywatcher Alt/Az Mount
	ID_SKYWATCHER_EQ(3), // Skywatcher EQ Mount
	ID_ORION_EQG(4), // Orion EQ Mount
	ID_ORION_TELETRACK(5), // Orion TeleTrack Mount
	ID_EQ_EMULATOR(6), // EQ Mount Emulator
	ID_AZ_EMULATOR(7), // Alt/Az Mount Emulator
	ID_NEXSTARGT80(8), // NexStarGT-80 mount
	ID_NEXSTARGT114(9), // NexStarGT-114 mount
	ID_STARSEEKER80(10), // NexStarGT-80 mount
	ID_STARSEEKER114(11); // NexStarGT-114 mount

	private final int id;
	MOUNTID(int id) {
		this.id = id;
	}
}
