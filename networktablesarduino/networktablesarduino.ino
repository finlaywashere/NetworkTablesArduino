#include <Ethernet.h>

byte mac[] = {
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED
};
IPAddress server(192,168,33,35);

EthernetClient client;

void setup() {
  Ethernet.init(10);
  Ethernet.begin(mac);
  Serial.begin(9600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  // Check for Ethernet hardware present
  if (Ethernet.hardwareStatus() == EthernetNoHardware) {
    Serial.println("Ethernet shield was not found.  Sorry, can't run withclient hardware. :(");
    while (true) {
      delay(1); // do nothing, no point running withclient Ethernet hardware
    }
  }
  while (Ethernet.linkStatus() == LinkOFF) {
    Serial.println("Ethernet cable is not connected.");
    delay(500);
  }
  // give the Ethernet shield a second to initialize:
  delay(1000);
  Serial.println("connecting...");

  // if you get a connection, report back via serial:
  if (client.connect(server, 1735)) {
    Serial.println("connected");
  } else {
    // if you didn't get a connection to the server:
    Serial.println("connection failed");
    while(1){
      ;
    }
  }
  // Send client hello
  client.write((char)0x1);
  client.write((char)0x3);
  client.write((char)0x0);
  client.write((char)0x0);

  // Send client end hello
  client.write((char)0x5);

  addEntry("/test/test","test",0xFFFF,1);
  addEntry("/test/test2","test2",0xFFFF,1);
  
}

void loop() {
  char c = client.read();
  if(c == 0x10){
    long length = readUleb128();
    char buffer[length];
    client.read(buffer,length);
    char type = client.read();
    short id = (client.read() << 8) | client.read();
    short seq_num = (client.read() << 8) | client.read();
    Serial.write(buffer,length);
    Serial.print(":");
    if(type == 0x2){
      short length2 = (client.read() << 8) | client.read();
      char buffer2[length];
      client.read(buffer2,length2);
      Serial.write(buffer2,length2);
      Serial.print("\n");
    }
  }else if(c == 0x4){
    // Server hello
    // Ignore 2 bytes
    client.read();
    client.read();
  }
}
void addEntry(char* name, char* value, short id, short seq_num){
  // Code for add entry
  client.write((char)0x10);
  // Entry name length
  writeUleb128(strlen(name));
  // Entry name
  client.write(name);
  // Entry type code (String)
  client.write((char)0x2);
  // Id bytes
  client.write((char) 0xff);//(id & 0xff00));
  client.write((char) 0xff);//(id & 0xff));
  // Seq_num bytes
  client.write((char) (seq_num & 0xff00));
  client.write((char) (seq_num & 0xff));
  // Flags (0x0 for default, 0x1 for persistent)
  client.write((char)0x0);
  // Entry value length
  writeUleb128(strlen(value));
  // Entry value
  client.write(value);
}
void writeUleb128(long value){
  int length = 0;
  long value2 = value;
  while(value2 != 0){
    value2 = value2 >> 7;
    length++;
  }
  char bytes[length];
  int pos = length - 1;
  do{
    char c = value & 0x7f;
    value = value >> 7;
    if(value != 0)
      c |= 0x80;
    bytes[pos] = c;
    pos--;
  }while(value != 0);
  for(int i = 0; i < length; i++){
    client.write(bytes[i]);
  }
}
long readUleb128(){
  long result = 0;
  int shift = 0;
  while(true){
    char c = client.read();
    result |= (c & 0x7f) << shift;
    shift += 7;
    if((c & 0x80) == 0)
      break;
  }
  return result;
}
