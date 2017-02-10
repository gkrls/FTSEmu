package nl.vu.cs.tcs.tds.util;

public class Tuple<T, U> {
	final private T first;
	final private U second;
	
	public Tuple(T a, U b){
		this.first = a;
		this.second = b;
	}
	
	public T get1(){
		return first;
	}
	
	public U get2(){
		return second;
	}
	
	public static final <T, U>  Tuple of(final T first, final U second){
		return new Tuple<T, U>(first, second);
	}
	
	
}
