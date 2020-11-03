package xyz.finlaym.networktables;

import java.io.OutputStream;
import java.net.Socket;

public class POC {
	public static void main(String[] args) throws Exception{
		Socket s = new Socket("localhost",1735);
		OutputStream out = s.getOutputStream();
		// Send client header
		//out.write(new byte[] {0x6});
		
		// Send client hello
		out.write(new byte[] {0x1,0x0,0x1});
		
		// Send client end hello
		//out.write(new byte[] {0x5});
		
		// Send test entry
		out.write(new byte[] {0x10,0x0,0x6});
		out.write("client".getBytes());
		out.write(new byte[] {0x2,0x0,0x1,0x0,0x1,0x0,0x4});
		out.write("test".getBytes());
		
		while(true) {
			byte[] buf = new byte[1];
			s.getInputStream().read(buf);
			switch(buf[0]) {
			case 0x10:
				buf = new byte[2];
				s.getInputStream().read(buf);
				int length = (buf[0] << 8) + buf[1];
				buf = new byte[length];
				s.getInputStream().read(buf);
				String name = new String(buf);
				buf = new byte[5];
				s.getInputStream().read(buf);
				int type = buf[0];
				int id = (buf[1] << 8) + buf[2];
				int seq_num = (buf[3] << 8) + buf[4];
				
				switch(type) {
				case 0x2:
					buf = new byte[2];
					s.getInputStream().read(buf);
					length = (buf[0] << 8) + buf[1];
					buf = new byte[length];
					s.getInputStream().read(buf);
					String value = new String(buf);
					System.out.println(id+" : "+seq_num+" : "+name+" : "+value);
					break;
				}
				
				break;
			}
		}
	}
}
