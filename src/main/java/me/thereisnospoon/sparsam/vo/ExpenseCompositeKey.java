package me.thereisnospoon.sparsam.vo;

public class ExpenseCompositeKey {

	private String uniqueKey;
	private String username;

	public ExpenseCompositeKey() {}

	public ExpenseCompositeKey(String uniqueKey, String username) {
		this.uniqueKey = uniqueKey;
		this.username = username;
	}

	public String getUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(String uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
