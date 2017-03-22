
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

import message.Message;

public class Communicator {

	private boolean stop = false;
	
	private ByteBuffer recvBuffer;
	private ByteBuffer sendBuffer;
	
	private SocketHandler socketHandler;
	private MessageHandler messageHandler;
	
	class SocketHandler extends Thread{
		
		private Address serverAddress;
		private SocketChannel socketChannel;
				
		public SocketHandler(Address serverAddress){
			this.serverAddress = serverAddress;
			try {
				socketChannel = SocketChannel.open();
				socketChannel.configureBlocking(true);
			} catch (IOException e) {
				System.err.println("Socket open failed");
				System.exit(0);
			}
		}
		
		@Override
		public void run() {
			try {
				socketChannel.connect(new InetSocketAddress(serverAddress.getIP(), serverAddress.getPort()));
			} catch (IOException e2) {
				System.err.println("connect failed");
				System.exit(0);
			}

			// Read and write data.
			try {
				while (!stop) {

					//TODO Lock mechanism
					// receive data from server
					int readCount = socketChannel.read(recvBuffer);
					
					//server closes socket.
					if (readCount == -1) {
						try { socketChannel.close(); } catch (IOException e) { e.printStackTrace(); }
						return;
					}
					
					//Debug Message
					if (readCount > 0){
						System.out.println("Received Message : " + new String(recvBuffer.array(), 0, readCount + 1, "UTF-8"));
					}
					
					//TODO Lock Mechanism
					if(sendBuffer.hasRemaining()){
						socketChannel.write(sendBuffer);
						sendBuffer.clear();
					}
				}

				// socket close gracefully
				socketChannel.shutdownOutput();
				
				// wait until server close socket
				//  TODO set time limit
				while (socketChannel.read(recvBuffer) != -1) { 
					//intentional blank
				}
				
				socketChannel.close();

			} catch (ClosedByInterruptException e) {
				// socket close gracefully
				try {
					socketChannel.shutdownOutput();
					// wait until server close socket
					//  TODO set time limit
					while (socketChannel.read(recvBuffer) != -1) { 
						//intentional blank
					}
					
					socketChannel.close();
					
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} catch (IOException e1) {
				System.err.println("IO failed");
				try { socketChannel.close(); } catch (IOException e) { e.printStackTrace();	}
			}
		}
	};
	
	class MessageHandler extends Thread{
		
		private void processMessages() {
			//Deserialize MessageHeader
			//Deserialize Message
			//buffer flip ? compact?
			//processMessage
		}
	
		private void processMessage(Message msg) {
			
		}
		
		@Override
		public void run() {
			//TODO Lock Mechanism
			//read from recvBuffer
			//process messages
		}
	};	


	public Communicator(Address serverAddress) {		
		socketHandler = new SocketHandler(serverAddress);
		messageHandler = new MessageHandler();
				
		// typical socket buffer size 8K
		recvBuffer = ByteBuffer.allocateDirect(0x2000).order(ByteOrder.nativeOrder());
		sendBuffer = ByteBuffer.allocateDirect(0x2000).order(ByteOrder.nativeOrder());
	}

	public void communicatorRun() {		
		socketHandler.start();
		messageHandler.start();
	}

	public void setStop() {
		stop = true;
	}
	
	public boolean isAlive(){
		return ( socketHandler != null && socketHandler.isAlive() ) || ( messageHandler != null && messageHandler.isAlive() );
	}
	
	public void interrupt(){
		
		if(socketHandler != null)
			socketHandler.interrupt();
		
		if(messageHandler != null)
			messageHandler.interrupt();	
	
	}
	
	public void join() throws InterruptedException {
		
		if(socketHandler != null)
			socketHandler.join();
			
		if(messageHandler != null)
			messageHandler.join();		
	
	}
	
}
