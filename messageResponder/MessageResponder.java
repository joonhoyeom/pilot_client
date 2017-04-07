package messageResponder;

import message.Command;
import message.MessageHeader;

public abstract class MessageResponder {

	public static MessageResponder newMessageResponder(MessageHeader mh) {
		int command = mh.getCommand();

		switch (command) {
			case Command.DIR:
				return new DirResponder();
			
			case Command.RENAME:
				return new RenameResponder();
			
			case Command.MOVE:
				return new MoveResponder();
			
			case Command.COPY:
				return new CopyResponder();

			case Command.DELETE:
				return new DeleteResponder();

			case Command.FILEUP:
				return new FileUpResponder();

			default:
				return null;
		}
	}

	public abstract Object respond(Object messageBody);
}

