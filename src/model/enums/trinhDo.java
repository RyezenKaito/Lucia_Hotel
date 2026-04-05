package model.enums;

public enum trinhDo {
	THCS("Trung học cơ sở"),
	THPT("Trung học phổ thông"),
	CAODANG("Cao đẳng"),
	DAIHOC("Đại học"),
	TREN_DAIHOC("Trên đại học");

	private final String displayName;

	trinhDo(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
