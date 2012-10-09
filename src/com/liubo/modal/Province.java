package com.liubo.modal;

public class Province {
	private final String name;
	private final String code;
	public Province(String name, String code) {
		this.name = name;
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public String getCode() {
		return code;
	}
	@Override
	public String toString() {
		return "name[" + name + "],code[" + code + "]";
	}
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}
