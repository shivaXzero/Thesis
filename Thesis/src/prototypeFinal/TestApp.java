package prototypeFinal;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestApp {

	public static void main(String[] args) throws UnknownHostException {
		// TODO Auto-generated method stub
//		EmotionDialog dialog = new EmotionDialog();
//		dialog.start();
		List<String> s = new ArrayList<>();
		s.add("happy");
		s.add("sad");
		s.add("fear");
		s.add("angry");
		List<String> n = s;
		String a="";
		for(String i:n){
			a += i+ ",";
		}
		a= a.substring(0, a.length() -1);
		System.out.println(a);
	}
	
	void test() throws UnknownHostException{
		System.out.println(FileSystems.getDefault().getPath("C:/Users/") +"/"+ System.getProperty("user.name") +"/Desktop/Model");
		System.out.println(FileSystems.getDefault());
		System.out.println(System.getProperty("user.name"));
		System.out.println(InetAddress.getLocalHost().getHostName());
		System.out.println(System.getenv("USER"));
		
		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString() +"/Files";
		System.out.println("Current relative path is: " + s.replaceAll("\\\\", "/"));
		File theDir = new File(s.replaceAll("\\\\", "/"));
		System.out.println(theDir.toString().replaceAll("\\\\", "/"));
		Random rn = new Random();
    	int randNum = (rn.nextInt(5) + 1) * 1000;
    	
    	System.out.println(randNum);
	}

}
