package messageResponder;

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
	
	class MetaData{
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
		
		if("".equals(uri) || uri == null)
			uri = HOMEPATH;
		
		Path path = Paths.get(uri);
		String resultJSON = null;
		if(Files.notExists(path)){
			return "".getBytes();
		}
		
		try {
			DirectoryStream<Path> dirStream = Files.newDirectoryStream(path);
			ArrayList<MetaData> list = new ArrayList<MetaData>();
			for(Path i : dirStream){
				MetaData metaData = new MetaData();
				
				metaData.name = i.getFileName().toString();
				metaData.time = Files.getLastModifiedTime(path).toString();				
				metaData.fileType = getFileType(i);
				if(Files.isRegularFile(i))
					metaData.size = Files.size(i); //why IOException?
				else
					metaData.size = 0;
				list.add(metaData);
			}
			resultJSON = generateJSON(list);
		} catch (IOException e) {
			System.err.println("DirResponder : IOException");
			return "".getBytes();
		}				
		return charset.encode(resultJSON).array();
	}
	
	private String getFileType(Path p) {

		String fileExtension = null;

		if (Files.isDirectory(p)) {
			return "dir";
		}
		fileExtension = p.toString();
		if (fileExtension.contains(".")) {
			fileExtension = fileExtension.substring(fileExtension.lastIndexOf("."));
			if(fileExtension.length() <= 1)
				fileExtension = "bin";
			else
				fileExtension = fileExtension.substring(1);
		}else
			return "bin";
		
		return fileExtension;
	}
	
	private String generateJSON(List<MetaData> list){
		String jsonArray = "[ ";
		
		for(MetaData i : list){
			jsonArray = jsonArray + i.toString() + " ,\n";
		}
		jsonArray = jsonArray.substring(0, jsonArray.length()-2);
		return jsonArray + " ]";
	}
}
