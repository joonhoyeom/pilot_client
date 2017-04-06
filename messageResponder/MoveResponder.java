package messageResponder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//not complete!!
public class MoveResponder extends MessageResponder {
	@Override
	public Object respond(Object messageBody) {
//		Charset charset = Charset.forName("UTF-8");
//		String []strs = charset.decode(ByteBuffer.wrap((byte[])messageBody)).toString().split("\n");
//
//		if(strs.length < 2)
//			return false;
//
//		Path src = Paths.get(strs[0]);
//		Path targ= Paths.get(strs[1]);
//
//		try {
//			Files.move(src, targ);
//		} catch(FileAlreadyExistsException e1){
//			e1.printStackTrace();
//			return false;
//		} catch (IOException e2) {
//			// TODO Auto-generated catch block
//			e2.printStackTrace();
//			return false;
//		}
//		return true;
//	}
		return null;}
}
