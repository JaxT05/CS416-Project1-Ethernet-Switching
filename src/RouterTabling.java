import java.util.HashMap;
import java.util.Map;

public class RouterTabling {
    //in iteration 2, the tables will be hardcoded
    public static Map<String, String> returnForwardingTable(String routerID) {
        Map <String, String> forwardingTable = new HashMap<>();

        if (routerID.equalsIgnoreCase("R1")) {
            forwardingTable.put("net1", "net1.S1");
            forwardingTable.put("net2", "net2.R2");
            forwardingTable.put("net3", "net2.R2");
        }
        else if (routerID.equalsIgnoreCase("R2")) {
            forwardingTable.put("net1", "net2.R1");
            forwardingTable.put("net2", "net2.R1");
            forwardingTable.put("net3", "net3.S2");
        }

        return forwardingTable;
    }
}
