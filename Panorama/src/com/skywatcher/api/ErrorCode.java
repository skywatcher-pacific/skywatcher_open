package com.skywatcher.api;

public enum ErrorCode {

	ERR_INVALID_ID(1), // 無效的望遠鏡代碼 // Invalid mount ID
	ERR_ALREADY_CONNECTED(2), // 已經連接到另外一個ID的望遠鏡 // Already connected to another
								// mount ID
	ERR_NOT_CONNECTED(3), // 尚未連接到望遠鏡 // Telescope not connected.
	ERR_INVALID_DATA(4), // 無效或超範圍的資料 // Invalid data, over range etc
	ERR_SERIAL_PORT_BUSY(5), // 串口忙 // Serial port is busy.
	ERR_NORESPONSE_AXIS1(100), // 望遠鏡的主軸沒有回應 // No response from axis1
	ERR_NORESPONSE_AXIS2(101), // 望遠鏡的次軸沒有回應
	ERR_AXIS_BUSY(102), // 暫時無法執行該操作
	ERR_MAX_PITCH(103), // 目標位置仰角過高
	ERR_MIN_PITCH(104), // 目標位置仰角過低
	ERR_USER_INTERRUPT(105), // 用戶強制終止
	ERR_ALIGN_FAILED(200), // 校準望遠鏡失敗
	ERR_UNIMPLEMENT(300), // 未實現的方法
	ERR_WRONG_ALIGNMENT_DATA(400); // The alignment data is incorect.

	private final int id;
	ErrorCode(int id) {
		this.id = id;
	}
}
