package messageResponder;

import utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DeleteResponder extends MessageResponder {
	@Override
	public Object respond(Object messageBody) {
		Charset charset = Charset.forName("UTF-8");
		String targ = charset.decode(ByteBuffer.wrap((byte[])messageBody)).toString();

		targ = targ.trim();
		if("".equals(targ) || targ == null)
			return null;

		Path path = Paths.get(targ);

		try {
			Utils.removeRecursive(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
