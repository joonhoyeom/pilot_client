import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Scanner;

public class Main {
	
	public static Address getServerAddress(String configFilePath){
		
		InputStream is = null;
    	String ipstring = "";

    	try {
    		
    		is = new FileInputStream(configFilePath);
    		Reader reader = new InputStreamReader(is);
        	BufferedReader br = new BufferedReader(reader);
    	        
    		ipstring = br.readLine();
		
			br.close();
			reader.close();
			is.close();
		
    	} catch (FileNotFoundException e) {
    		System.out.println(configFilePath + " : ServerAddress.txt not found");
    		System.exit(0);
    	} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
    	}
    	
    	return new Address(ipstring);
	}
	
    public static void main(String[] args) throws InterruptedException {
    	Address serverAddress = getServerAddress("ServerAddress.txt");
    	
    	//Run CommandHandler thread
    	Communicator communicator = new Communicator(serverAddress);
  	
    	communicator.communicatorRun();
    	
		System.out.println("If you want to shut down, press \'y\'");    	
    	Scanner s = new Scanner(System.in);
    	String str;
    	do { 
    		str = s.next(); 
    	} while ( "y".equals(str) == false );
		
    	communicator.setStop();
    	try { Thread.sleep(300); } catch (InterruptedException e1) {}
    	
    	if(communicator.isAlive()){
    		System.out.println("Communicator threads are still running, it will be interrupted");
    		communicator.interrupt();
    	}
    	try { communicator.join(); } catch (InterruptedException e) { e.printStackTrace(); }
    }    
}
