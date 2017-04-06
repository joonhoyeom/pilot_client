package message;

public abstract class MessageBody {

	/**
	 * process message and return response MessageBody
	 * if there are no response message, return null
	 * */
	abstract public int getSerializedSize();
	abstract public byte[] serialize();
	abstract public MessageBody deserialize(byte [] serializedData);
}
