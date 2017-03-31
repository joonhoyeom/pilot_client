
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

import message.MessageHeader;
import messageResponder.MessageResponder;
import utils.Utils;


public class Communicator {

	class SocketHandler extends Thread{
		
		private Address serverAddress;
		private SocketChannel socketChannel;
		private Selector selector;

		//Initialize address to connect, create socket as blocking
		public SocketHandler(Address serverAddress){
			this.serverAddress = serverAddress;
			try {
				selector = Selector.open();
				socketChannel = SocketChannel.open();
				
				socketChannel.configureBlocking(false);
				socketChannel.connect(new InetSocketAddress(serverAddress.getIP(), serverAddress.getPort()));
				int operations = SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE;
				socketChannel.register(selector, operations);
			
			} catch (IOException e) {
				System.err.println("[SocketHandler] : IOException");
			}
		}
				
		private boolean tryConnect(SelectionKey key) throws IOException{
			SocketChannel channel = (SocketChannel) key.channel();
		    while (channel.isConnectionPending()) {
		      channel.finishConnect();
		    }
		    return true;
		}
		
		@Override
		public void run() {
			// Read and write data.
			try {
				while (!stop) {
					
					if(selector.select(1000) > 0){
						Set<SelectionKey> selectedKeys = selector.selectedKeys();
						Iterator iterator = selectedKeys.iterator(); 
						
						while(iterator.hasNext()){
							SelectionKey key = (SelectionKey)iterator.next();
							iterator.remove();
							
							if(key.isConnectable()){
								if(tryConnect(key) == false){
									System.out.println("Connect failed");
									return ;
								}
							}
							
							if(key.isReadable()){
								
								int readCount = 0;
								synchronized (recvBufferMutex) {
									//compact?
									
									readCount = socketChannel.read(recvBuffer);
								}
								
								if(readCount == -1){
									System.out.println("Server closed");
									socketChannel.close();
									stop = true;
									return ;
								}
							}
							
							if(key.isWritable()){
								synchronized (sendBufferMutex) {
									if(sendBuffer.position() > 0){
										sendBuffer.flip();
										socketChannel.write(sendBuffer);
										sendBuffer.clear();
									}
								}
							}
						}				
					}					
				}

				// socket close gracefully
				socketChannel.shutdownOutput();
				
				// wait until server close socket
				//  TODO set time limit
				synchronized (recvBufferMutex) {
					while (socketChannel.read(recvBuffer) != -1) { 
						//intentional blank
					}
				}				
				
				socketChannel.close();
			} catch (ClosedByInterruptException e) {
				// socket close gracefully
				try {
					if(socketChannel.isOpen()){
						socketChannel.shutdownOutput();
						System.out.println("shutdown called by interrupt");
						// wait until server close socket
						//  TODO set time limit
						synchronized (recvBufferMutex) {
							while (socketChannel.read(recvBuffer) != -1) { 
								//intentional blank
							}
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
	
	class MessageHandler extends Thread{
		
		private void processMessages() {
			byte[] recvBufferCopy = null;
					
			//fetch messages from recvBuffer
			synchronized (recvBufferMutex) {
				
				if(recvBuffer.position() > 0){
					recvBufferCopy = new byte[recvBuffer.position()];
					System.arraycopy(recvBuffer.array(), 0, recvBufferCopy, 0, recvBufferCopy.length);
					recvBuffer.clear();
															
					int i = 0;
					while(true){
						int headerPos = Utils.indexOf(recvBufferCopy, MessageHeader.messageStart, i);
						
						if(headerPos == -1){
							System.out.println("No MessageStart");
							break;
						}
						System.out.println("Got messageStart");
						//Header is not arrived yet
						if (recvBufferCopy.length - headerPos < MessageHeader.serializedSize) {
							System.out.println("Invalid Header");
							
							// put last header into buffer back
							recvBuffer.put(recvBufferCopy, headerPos, recvBufferCopy.length - headerPos);
							break;
						}
						//Unserialize MessageHeader
						MessageHeader header = new MessageHeader(recvBufferCopy, headerPos);
						int messageBodyStart = headerPos + MessageHeader.serializedSize;
						int messageBodyEnd = headerPos + MessageHeader.serializedSize + header.getMessageBodySize();
						
						//MessageBody is not arrived yet
						if(messageBodyEnd > recvBufferCopy.length){
							recvBuffer.put(recvBufferCopy, headerPos, recvBufferCopy.length - headerPos);
							break;
						}
						
						byte []messageBody = new byte [header.getMessageBodySize()];
						
						System.arraycopy(recvBufferCopy, messageBodyStart, messageBody, 0, header.getMessageBodySize());
						
						//Process Message
						//Incomplete code
						{
							MessageResponder mr = MessageResponder.newMessageResponder(header);
							if(mr != null){
								byte[] retVal = (byte[])(mr.respond(messageBody));
								synchronized (sendBufferMutex) {
									sendBuffer.put(retVal);
								}
							} else {
								System.err.println("Invalid message header");
							}							
						}
						i = messageBodyEnd;
					}
				}
				else //there are no messages
					return;
			}
				
		}
	
		@Override
		public void run() {
			while( !stop ){
				processMessages();
			}
		}
	};	
	
	private boolean stop = false;
	
	private ByteBuffer recvBuffer;
	private Object 	   recvBufferMutex;
	
	private ByteBuffer sendBuffer;
	private Object	   sendBufferMutex;
	
	private SocketHandler socketHandler;
	private MessageHandler messageHandler;
	

	public Communicator(Address serverAddress) {		
		socketHandler = new SocketHandler(serverAddress);
		messageHandler = new MessageHandler();
		
		recvBufferMutex = new Object();
		sendBufferMutex = new Object();
		
		// typical socket buffer size 8K
		recvBuffer = ByteBuffer.allocate(0x2000);
		sendBuffer = ByteBuffer.allocate(0x2000);
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
