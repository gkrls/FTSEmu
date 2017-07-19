package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Option {
	private int id;
	private String opt;
	private boolean expectValue;
	private int value;
	private String alias;
	
	
	public Option(int id, String opt, boolean expectValue, int value, String alias){
		this.id = id;
		this.opt = opt;
		this.expectValue = expectValue;
		this.value = value;
		this.alias = alias;
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
	
	public String alias() {
	    return this.alias;
	}
}
