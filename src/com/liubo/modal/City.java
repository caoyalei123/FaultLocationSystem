package com.liubo.modal;

public class City {
	private final String name;
	private final String code;
	private final String pcode;
	public City(String name, String code, String pcode) {
		this.name = name;
		this.code = code;
		this.pcode = pcode;
	}
	public String getName() {
		return name;
	}
	public String getCode() {
		return code;
	}
	public String getPcode() {
		return pcode;
	}
	@Override
	public String toString() {
		return "name[" + name + "],code[" + code + "],pcode[" + pcode + "]";
	}
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}
