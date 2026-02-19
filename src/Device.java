import java.util.Map;
import java.util.Scanner;

public class Device {
    String ID;
    String Port;
    String IP;
    String GatIP;
    String GatIP2;
    String VirIP;

    public Device(String ID, String port, String IP, String gatIP, String gatIP2, String virIP) {
        this.ID = ID;
        Port = port;
        this.IP = IP;
        GatIP = gatIP;
        GatIP2 = gatIP2;
        VirIP = virIP;

    }

    public Device(String ID, String port, String IP,  String gatIP, String virIP) {
        this.ID = ID;
        Port = port;
        this.IP = IP;
        GatIP = gatIP;
        this.VirIP = virIP;
    }

    public Device(String ID, String port, String IP) {
        this.ID = ID;
        Port = port;
        this.IP = IP;
    }


}