
public class Address {
	
	String 	ip;
	short 	port;
		
	public Address(String ip, short port){
		this.ip = ip;
		this.port = port;
	}
	
	//expected format : 192.168.22.22:8000
	public Address(String address){
		String [] strs = address.split(":");
		
		if( strs.length != 2){
			//error
			ip = "";
			port = 0;
			return;
		}
		
		//To do : validity check
		ip = strs[0];
		port = Short.parseShort(strs[1]);
	}
	
	public String 	getIP() 			{ return ip;	}
	public void		setIP(String ip) 	{ this.ip = ip; }
	
	public short 	getPort() 			{ return port; }
	public void 	setPort(short port) { this.port = port;	}

	@Override
	public String toString() {
		return "Address [ip=" + ip + ", port=" + port + "]";
	}	
}
