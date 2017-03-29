
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;

public class Communicator {

	class SocketHandler extends Thread{
		
		private Address serverAddress;
		private SocketChannel socketChannel;
		
		//Initialize address to connect, create socket as blocking
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
			
			//connect to server
			try {
				socketChannel.connect(new InetSocketAddress(serverAddress.getIP(), serverAddress.getPort()));
			} catch (IOException e2) {
				System.err.println("connect failed");
				return;
			}

			// Read and write data.
			try {
				while (!stop) {

					// receive data from server
					int readCount = 0;
					
					readCount = socketChannel.read(recvBuffer);	
					
					//server closes socket.
					if (readCount == -1) {
						try { socketChannel.close(); } catch (IOException e) { e.printStackTrace(); }
						return;
					}
					
					//Debug Message
					if (readCount > 0){
						System.out.println("[Received Message] " + recvBuffer.asCharBuffer().toString());
						//processMessage(recvBuffer);
					}
					
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
					if(socketChannel.isOpen()){
						socketChannel.shutdownOutput();
						// wait until server close socket
						//  TODO set time limit
						while (socketChannel.read(recvBuffer) != -1) { 
								//intentional blank
						}
						socketChannel.close();
					}					
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} catch (IOException e1) {
				System.err.println("IO failed");
				try { socketChannel.close(); } catch (IOException e) { e.printStackTrace();	}
			}
		}
	};
	
	
	private boolean stop = false;
	
	private ByteBuffer recvBuffer;
//	private Object 	   recvBufferMutex;
	
	private ByteBuffer sendBuffer;
//	private Object	   sendBufferMutex;
	
	private SocketHandler socketHandler;
	

	//Initialize thread classes, mutex, send receive buffer
	public Communicator(Address serverAddress) {		
		socketHandler = new SocketHandler(serverAddress);
		
//		recvBufferMutex = new Object();
//		sendBufferMutex = new Object();
		
		// typical socket buffer size 8K
		recvBuffer = ByteBuffer.allocateDirect(0x2000).order(ByteOrder.nativeOrder());
		sendBuffer = ByteBuffer.allocateDirect(0x2000).order(ByteOrder.nativeOrder());
	}

	public void communicatorRun() {		
		socketHandler.start();
	}

	public void setStop() {
		stop = true;
	}
	
	public boolean isAlive(){
		return ( socketHandler != null && socketHandler.isAlive() ); //|| ( messageHandler != null && messageHandler.isAlive() );
	}
	
	public void interrupt(){		
		if(socketHandler != null)
			socketHandler.interrupt();
	}
	
	public void join() throws InterruptedException {		
		if(socketHandler != null)
			socketHandler.join();
	}
	
}