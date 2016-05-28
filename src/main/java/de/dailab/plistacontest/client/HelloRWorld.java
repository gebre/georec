package de.dailab.plistacontest.client;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;

public class HelloRWorld {
	static Rengine re; // initialized in constructor or autowired
	
	public static void main(String [] args){
		re.eval(String.format("greeting <- '%s'", "Hello R World"));
		REXP result = re.eval("greeting");
		System.out.println("Greeting from R: "+result.asString());
	}
	
	
}