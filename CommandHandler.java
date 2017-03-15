import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

//		RENAME,
//		MOVE = COPY + PASTE + DELETE,
//		COPY,
//		PASTE,
//		DELETE,
//		UPLOAD,
//		DOWNLOAD

/*
 * ############ Application Protocol
 * 
 * JSON Format
 * 
 * ====== Server Side ======                             ====== Client Side ======
 * 
 * {													{
 * 	command : DIR											 
 *  path : 												
 * }
 * 
 * {
 *  command : RENAME
 *  src :
 *  dest :
 * }
 * 
 * {
 *  command : COPY
 *  src : 
 * }
 * 
 * {
 *  command : PASTE
 *  dest : 
 * }
 * 
 * {
 *  command : DELETE
 *  targ : 
 * }
 * 
 * {
 * 	command : UPLOAD
 *  dest : 
 *  data : 
 * }
 * 
 * {
 * 	command : DOWNLOAD
 * 	targ : 
 * }
 * 
 * 
 * */

public class CommandHandler extends Thread {

	private boolean stop = false;
	private Address serverAddress;
	private Socket socket;
	private byte[] recvBuffer;

	public CommandHandler(Address serverAddress) {
		this.serverAddress = serverAddress;
		socket = null;
		recvBuffer = new byte[1024];
	}

	public void connectToServer() throws IOException {
		socket = new Socket();
		socket.connect(new InetSocketAddress(serverAddress.getIp(), serverAddress.getPort()), 0);
	}

	@Override
	public void run() {
		
		//Connection is not established.
		if (socket == null || socket.isConnected() ==false) {
			System.out.println("Call \'connectToServer\' before run");
			return;
		}		

		//Read and write data.
		try {
			OutputStream os = socket.getOutputStream();
			InputStream is = socket.getInputStream();

			while (!stop) {

				int readCount = is.read(recvBuffer);	
				if(readCount == -1){
					try { socket.close(); } catch (IOException e) {	e.printStackTrace(); }
					return ;
				}
				System.out.println("Received Message : " + new String(recvBuffer, 0, readCount+1, "UTF-8"));
						
				os.write(("Client Send Message\n").getBytes("UTF-8"));
				os.flush();
			}
			//todo : close socket gracefully
			//close send buffer.
		} catch (IOException e1) {
			System.err.println("IO failed");
			try { socket.close(); } catch (IOException e) {	e.printStackTrace(); }
			System.exit(0);
		} 
	
		try { 
			if(!socket.isClosed()) 
				socket.close(); 
		} catch (IOException e) {
			e.printStackTrace(); 
		}
		
	}

	public void setStop() {
		stop = true;
	}
}
