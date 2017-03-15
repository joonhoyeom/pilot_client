import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
	 
    public static void main(String[] args) {
    	
    	//Read Server address    	
    	InputStream is = null;
    	    
    	String ipstring = "";
    	try {
    		
    		is = new FileInputStream("ServerAddress.txt");
    		Reader reader = new InputStreamReader(is);
        	BufferedReader br = new BufferedReader(reader);
    	        
    		ipstring = br.readLine();
		
			br.close();
			reader.close();
			is.close();
		
    	} catch (FileNotFoundException e) {
    		System.out.println("ServerAddress.txt not found");
    		return;
    	} catch (IOException e) {
			e.printStackTrace();
			return ;
    	}
    	
    	
    	Address serverAddress = new Address(ipstring);
    	System.out.println("## DEBUG ## Address" + serverAddress.toString());
  	    	 	   	
    	
    	//Run CommandHandler thread
    	CommandHandler commandHandler = new CommandHandler(serverAddress);
    	
    	try {
    		commandHandler.connectToServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	commandHandler.start();
    	
		System.out.println("If you want to shut down, press \'y\'");    	
    	Scanner s = new Scanner(System.in);
    	String str;
    	do { 
    		str = s.next(); 
    	} while ( "y".equals(str) == false );
		
    	commandHandler.setStop();
    	
    	try {
			commandHandler.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//send quit message and close input stream.
    	//terminate thread
    	
    	
    }    
}
