package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Option {
	private int id;
	private String opt;
	private boolean expectValue;
	private int value;
	
	
	public Option(int id, String opt, boolean expectValue, int value){
		this.id = id;
		this.opt = opt;
		this.expectValue = expectValue;
		this.value = value;
	}
	
	public String getName(){
		return this.opt;
	}
	
	public int value(){
		return this.value;
	}
	
	public void setValue(int value){
		this.value = value;
	}
	
	public int getId(){
		return this.id;
	}
	
	@Override
	public String toString(){
		return opt + "\t" + value;
	}
}
