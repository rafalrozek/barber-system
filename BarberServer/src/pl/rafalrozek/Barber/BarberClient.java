package pl.rafalrozek.Barber;

import pl.rafalrozek.Controller;

import java.io.*;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarberClient extends Thread{
    private SocketAddress address;
    private Controller ctrl;
    private DataOutputStream dos;
    private DataInputStream dis;
    private List <DataOutputStream> socketList;
    private Map <String, HashMap<Integer, Boolean>> visits = new HashMap<String, HashMap<Integer, Boolean>>();


    public BarberClient(Socket cs, Controller ctrl, List<DataOutputStream> socketList, Map <String, HashMap<Integer, Boolean>>  visits){
        this.ctrl = ctrl;
        this.socketList = socketList;
        this.visits = visits;
        this.address = cs.getRemoteSocketAddress();

        OutputStream out = null;
        InputStream in = null;
        try {
            out = cs.getOutputStream();
            in = cs.getInputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }

        dos  = new DataOutputStream(out);
        dis  = new DataInputStream(in);

        socketList.add(dos);

        System.out.println(address + " connected.");

        //update all clients
        updateClientVisits();
    }


    public void run() {
        while(true){
            int requestType = -1;
            try {
                requestType = dis.readInt();
            } catch (IOException e) {
                socketList.remove(dos);
                System.err.println(this.address + " disconnected.");
                break;
            }
            //add visit
            if(requestType == 1){
                addVisit();
            }
            //remove visit
            if(requestType == 2){
                removeVisit();
            }
        }

    }

    private void removeVisit() {
        String date = "";
        int hour = -1;
        try {
            date = dis.readUTF();
            hour = dis.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Removed visit on "+ date + " (" + hour +")");
        visits.get(date).put(hour, false);
        ctrl.visitsListRemoveItem(date + " " + hour + ":00");

        for(DataOutputStream out : socketList){
            try {
                out.writeInt(2); // flag
                out.writeUTF(date);
                out.writeInt(hour);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    private void addVisit() {
        String date = "";
        int hour = -1;
        try {
            date = dis.readUTF();
            hour = dis.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Adding visit on "+ date + " (" + hour +")");
        if(visits.get(date) == null){
            HashMap<Integer,Boolean> hm = new HashMap<>();
            hm.put(hour, true);
            visits.put(date, hm);
        }
        else{
            visits.get(date).put(hour, true);
        }
        ctrl.visitsListAddItem(date + " " + hour + ":00");


        for(DataOutputStream out : socketList){
            try {
                out.writeInt(1); // flag
                out.writeUTF(date);
                out.writeInt(hour);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    private void updateClientVisits(){
        visits.forEach((s, integerBooleanHashMap) -> {
            integerBooleanHashMap.forEach((integer, aBoolean) -> {
                if(aBoolean){
                    try {
                        dos.writeInt(1); // flag
                        dos.writeUTF(s);
                        dos.writeInt(integer);
                    }
                    catch (IOException e){
                        e.printStackTrace();
                    }
                }
            });
        });
    }
}
