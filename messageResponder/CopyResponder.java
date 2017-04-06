package messageResponder;

import message.MessageBody;
import message.MessageHeader;
import utils.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CopyResponder extends MessageResponder {
    @Override
    public Object respond(Object messageBody) {
        //byte messageBody to string
        Charset charset = Charset.forName("UTF-8");
        String[] strs = charset.decode(ByteBuffer.wrap((byte[]) messageBody)).toString().split("\n");

        if (strs.length == 2) {
            Path src = Paths.get(strs[0].trim());
            Path targ = Paths.get(strs[1].trim());
            try {
                Utils.copyRecursive(src, targ);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }
}
