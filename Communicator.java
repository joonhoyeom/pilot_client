
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import message.Message;

public class Communicator extends Thread {

	private boolean stop = false;
	private Address serverAddress;

	private Selector selector;
	private SocketChannel socketChannel;
	private ByteBuffer buffer;

	private void processMessages(byte[] recvBuffer) {

	}

	private void processMessage(Message msg) {

	}

	private void sendMessages() {

	}

	public Communicator(Address serverAddress) {
		this.serverAddress = serverAddress;
		try {
			selector = Selector.open();
			socketChannel = SocketChannel.open();
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_CONNECT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// typical socket buffer size 8K
		buffer = ByteBuffer.allocateDirect(0x2000).order(ByteOrder.nativeOrder());
	}

	@Override
	public void run() {

		try {
			selector.select();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		SelectionKey selectionKey = socketChannel.keyFor(selector);
		selectionKey.interestOps(SelectionKey.OP_READ);
		
		// Read and write data.
		try {
			while (!stop) {
				int readCount = socketChannel.read(buffer);
				if (readCount == -1) {
					try {
						socketChannel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					return;
				}
				if (readCount > 0)
					System.out.println("Received Message : " + new String(buffer.array(), 0, readCount + 1, "UTF-8"));

				System.out.println("Non-Block Debug Message");
				buffer.put("Test message from Client".getBytes());
				socketChannel.write(buffer);
				buffer.clear();
			}
			// TODO : close socket gracefully
			// close send buffer.
		} catch (IOException e1) {
			System.err.println("IO failed");
			try {
				socketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}

		try {
			if (socketChannel.isOpen())
				socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void setStop() {
		stop = true;
	}
}
