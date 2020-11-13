package pl.rafalrozek.Barber;

import pl.rafalrozek.Controller;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarberServer extends Thread{
    private ServerSocket s;
    public List<DataOutputStream> socketList = new ArrayList<>();
    private Map <String, HashMap<Integer, Boolean>>  visits = new HashMap<>() ;
    private Controller ctrl;


    public BarberServer(Controller ctrl) {
        this.ctrl = ctrl;

        try {
            s = new ServerSocket(1337);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Started server on port: 1337");

    }

    @Override
    public void run() {
        while(true){
            try {
                Socket cs = s.accept();
                BarberClient barberClient = new BarberClient(cs, ctrl, socketList, visits);
                barberClient.start();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
