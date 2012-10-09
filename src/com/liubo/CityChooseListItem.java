package com.liubo;

public class CityChooseListItem {
	private String name;
	private String pcode;
	public CityChooseListItem() {
		
	}
	public CityChooseListItem(String name, String code) {
		this.name = name;
		this.pcode = code;
	}
	public String getName(){
		return name;
	}
	public String getPcode(){
		return pcode;
	}
	public void setName(String name){
		this.name=name;
	}
	public void setPcode(String pcode){
		this.pcode=pcode;
	}
}
