package xyz.finlaym.networktables;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class POC {
	public static void main(String[] args) throws Exception{
		Socket s = new Socket("localhost",1735);
		OutputStream out = s.getOutputStream();
		
		// Send client hello
		out.write(new byte[] {0x1,0x03,0x00,0x00});
		out.write(new byte[] {0x5});
		
		// Send test entry
		//TODO: Get this working
		
		//out.write(new byte[] {0x10,0x0a, 0x2f, 0x74, 0x65, 0x73, 0x74, 0x2f, 0x74, 0x65, 0x73,
		//		0x74, 0x02, (byte) 0xff, (byte) 0xff, 0x00, 0x01, 0x00, 0x04, 0x74, 0x65, 0x73, 0x74
		//});
		addEntry(out, "/test/test2", "test", Integer.MAX_VALUE, 1);
		//out.write(new byte[] {0x11,0x0,0x0,0x0,0x2,0x2,0x0,0x4});
		//out.write("tset".getBytes("UTF-8"));
		
		while(true) {
			byte[] buf = new byte[1];
			s.getInputStream().read(buf);
			switch(buf[0]) {
			case 0x10:
				int length = (int) readUleb128(s.getInputStream());
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
			default:
				int code = (int) buf[0];
				System.err.println("Unrecognized code "+String.valueOf(code));
				break;
			}
		}
	}
	private static void addEntry(OutputStream out, String name, String value, int id, int seq_num) throws IOException {
		out.write(new byte[] {0x10});
		writeUleb128(out, name.length());
		byte[] nameB = name.getBytes();
		out.write(nameB);
		// Write type (String = 0x2) (8 bit), id (16 bit), sequence number (16 bit), and flags (0x0 is default, 0x1 is for persistent) (8 bit)
		out.write(new byte[] {0x2,(byte) 0xff,(byte) 0xff, (byte) (seq_num & 0xff00), (byte) (seq_num & 0xff),0x0});
		writeUleb128(out, value.length());
		byte[] valueB = value.getBytes();
		out.write(valueB);
	}
	private static long readUleb128(InputStream in) throws IOException {
		long result = 0;
		int shift = 0;
		while(true) {
			byte b = in.readNBytes(1)[0];
			result |= (b & 0x7f) << shift;
			shift += 7;
			if((b & 0x80) == 0)
				break;
		}
		return result;
	}
	private static void writeUleb128(OutputStream out, long value) throws IOException {
		
		int length = 0;
		long value2 = Long.valueOf(value);
		while(value2 != 0) {
			value2 = value2 >> 7;
			length++;
		}
		byte[] bytes = new byte[length];
		
		int pos = length-1;
		
		do {
			// Get the last 7 bits of this byte
			byte b = (byte) (value & 0x7f);
			// Shift value 7 bits over
			value = value >> 7;
			// If its not the last byte then mark it
			if(value != 0)
				b |= 0x80;
			bytes[pos] = b;
			pos--;
		}while(value != 0);
		out.write(bytes);
	}
}
