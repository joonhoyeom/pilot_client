package messageResponder;

import message.Command;
import message.MessageHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DirResponder extends MessageResponder {
	
	final static String HOMEPATH = ".";
//	final static String HOMEPATH = "/mnt/c/Users/joonho/Desktop";
	
	class FileMetaData{
		public String name;
		public String time;
		public String fileType;
		public long size;
		
		@Override
		public String toString() {
			return "{ \"name\" : " + "\"" + name+ "\"" + 
					", \"time\" : " + "\""+ time + "\"" + 
					", \"fileType\" : " + "\""+ fileType + "\""+ 
					", \"size\" : " + size + "}";
		}
	}
	
	@Override
	public Object respond(Object messageBody) {
		Charset charset = Charset.forName("UTF-8");
		String uri = charset.decode(ByteBuffer.wrap((byte[])messageBody)).toString();
		uri = uri.trim();

		//null uri
		if("".equals(uri) || uri == null)
			return null;

		Path path = Paths.get(uri);

		//Invalid uri
		if(Files.notExists(path))
			return null;

		String resultJSON = null;
		try {
			DirectoryStream<Path> dirStream = Files.newDirectoryStream(path);
			ArrayList<FileMetaData> list = new ArrayList<FileMetaData>();
			for(Path i : dirStream){
				FileMetaData metaData = new FileMetaData();
				
				metaData.name = i.getFileName().toString();
				metaData.time = Files.getLastModifiedTime(i).toString().replace("T", " ").substring(0, 19);
				metaData.fileType = getFileType(i);
				if(Files.isRegularFile(i))
					metaData.size = Files.size(i);
				else
					metaData.size = 0;
				list.add(metaData);
			}
			resultJSON = generateJSON(list);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		byte [] resultJSONByte = charset.encode(resultJSON).array();
		MessageHeader mh = new MessageHeader(Command.DIRRES, resultJSONByte.length);

		ByteBuffer message = ByteBuffer.allocate(MessageHeader.serializedSize + resultJSONByte.length);
		message.put(mh.getBytes());
		message.put(resultJSONByte);
		//JSON string to byte array
		return message.array();
	}

	private String getFileType(Path p) {
		String fileExtension = null;

		if (Files.isDirectory(p)) {
			return "dir";
		}
		fileExtension = p.toString();
		if (fileExtension.contains(".")) {
			fileExtension = fileExtension.substring(fileExtension.lastIndexOf("."));
			//end with dot(.) or there is no extension
			if(fileExtension.length() <= 1)
				fileExtension = "bin";
			else
				fileExtension = fileExtension.substring(1);
		}else
			return "bin";
		
		return fileExtension;
	}

	//MetaData array to JSON string
	private String generateJSON(List<FileMetaData> list){
		String jsonArray = "[ ";
		
		for(FileMetaData i : list){
			jsonArray = jsonArray + i.toString() + " ,\n";
		}
		jsonArray = jsonArray.substring(0, jsonArray.length()-2);
		return jsonArray + " ]";
	}
}
