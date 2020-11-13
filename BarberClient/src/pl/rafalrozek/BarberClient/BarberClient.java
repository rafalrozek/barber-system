package pl.rafalrozek.BarberClient;

import javafx.scene.control.*;
import javafx.util.Callback;
import pl.rafalrozek.Controller;

import java.io.*;
import java.net.Socket;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BarberClient extends Thread {
    private Controller ctrl;
    private boolean running;
    private Map<String, HashMap<Integer, Boolean>> visits = new HashMap<String, HashMap<Integer, Boolean>>();

    private String myDate = "";
    private Integer myHour = -1;

    private DataOutputStream dos;
    private DataInputStream dis;

    /*
        datepicker cell factory
     */
    final Callback<DatePicker, DateCell> dayCellFactory = new Callback<DatePicker, DateCell>() {
        public DateCell call(final DatePicker datePicker) {
            return new DateCell() {
                @Override public void updateItem(LocalDate item, boolean empty) {
                    super.updateItem(item, empty);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-LL-dd");
                    String formattedString = item.format(formatter);
                    Boolean isBusy = isDayBusy(formattedString);
                    if (isBusy) {
                        setStyle("-fx-background-color: #FFA500;");
                        HashMap<Integer, Boolean> map = visits.get(formattedString);
                        String freeHours = getFreeHours(map);
                        Boolean isfull = isFullDay(map);


                        setTooltip(new Tooltip(freeHours));

                        if(isfull){
                            setStyle("-fx-background-color: #FF0000;");
                            setTooltip(new Tooltip("Brak miejsc"));
                        }
                    } else {
                        setStyle("-fx-background-color: #008000;");
                        setTooltip(new Tooltip("Wolne wszystkie"));
                    }

                    //unselect checkbox
                    RadioButton selected = (RadioButton) ctrl.group.getSelectedToggle();
                    if(selected != null){
                        selected.setSelected(false);
                    }

                }
            };
        }
    };


    public BarberClient(String host, int port, Controller ctrl) {
        this.ctrl = ctrl;
        ctrl.date.setDayCellFactory(dayCellFactory);
        Socket s = null;
        OutputStream out = null;
        InputStream in = null;
        try {
            s = new Socket(host, port);
            out = s.getOutputStream();
            in = s.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }


        dos = new DataOutputStream(out);
        dis = new DataInputStream(in);

        ctrl.group.getToggles().stream().map((toggle) -> (RadioButton)toggle).forEach((button) -> {
            button.setDisable(true);
        });

        //on button click
        ctrl.send.setOnAction(e -> {
            if(ctrl.send.getText().equals("Zapisz sie")) {
                RadioButton selected = (RadioButton) ctrl.group.getSelectedToggle();
                if(selected != null && ctrl.date.getValue() != null) {
                    addVisit(ctrl.date.getValue().toString(), Integer.parseInt(selected.getText().substring(0, 2)));
                    ctrl.date.setDisable(true);
                    ctrl.send.setText("Usun zapis");
                    ctrl.group.getToggles().stream().map((toggle) -> (RadioButton) toggle).forEach((button) -> {
                        button.setDisable(true);
                    });
                }
            }
            else{

                RadioButton selected = (RadioButton) ctrl.group.getSelectedToggle();
                removeVisit(ctrl.date.getValue().toString(), Integer.parseInt(selected.getText().substring(0,2)));
                ctrl.date.setDisable(false);
                ctrl.send.setText("Zapisz sie");
                ctrl.group.getToggles().stream().map((toggle) -> (RadioButton)toggle).forEach((button) -> {
                    button.setDisable(false);
                });
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-LL-dd");
                String formattedString = ctrl.date.getValue().toString();

                for( Map.Entry<Integer, Boolean> entry : visits.get(formattedString).entrySet()){
                    if(entry.getValue()){
                        RadioButton r = (RadioButton)ctrl.group.getToggles().get(entry.getKey()-10);
                        r.setDisable(true);
                    }
                }
                selected.setDisable(false);
            }
        });

        //on date pick
        ctrl.date.valueProperty().addListener((observableValue, localDate, t1) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyy-LL-dd");
            String formattedString = t1.format(formatter);

            if( visits.get(formattedString) == null){
                ctrl.group.getToggles().stream().map((toggle) -> (RadioButton)toggle).forEach((button) -> {
                    button.setDisable(false);
                });

            }
            else {
                ctrl.group.getToggles().stream().map((toggle) -> (RadioButton)toggle).forEach((button) -> {
                    button.setDisable(false);
                });

                for (Map.Entry<Integer, Boolean> entry : visits.get(formattedString).entrySet()) {
                    RadioButton r = (RadioButton) ctrl.group.getToggles().get(entry.getKey() - 10);
                    if (entry.getValue()) {
                        r.setDisable(true);
                    } else {
                        r.setDisable(false);
                    }
                }
            }
        });
    }

    private void addVisit(String s, int i) {
        try {
            dos.writeInt(1); // flag
            dos.writeUTF(s);
            dos.writeInt(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
        myDate = s;
        myHour = i;
    }
    private void removeVisit(String s, int i) {
        try {
            dos.writeInt(2); // flag
            dos.writeUTF(s);
            dos.writeInt(i);
        } catch (IOException e) {
            e.printStackTrace();
        }
        myDate = "";
        myHour = -1;
    }

    public void run() {
        while (true) {
            int requestType = -1;
            try {
                requestType = dis.readInt();
            } catch (IOException e) {
                System.err.println("Server connection error.");
                break;
            }
            //add visit
            if(requestType == 1){
                updateAddVisit();
            }
            //remove visit
            if(requestType == 2){
                updateRemoveVisit();
            }

        }
    }

    private void updateRemoveVisit() {
        String date = "";
        int hour = -1;
        try {
            date = dis.readUTF();
            hour = dis.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        RadioButton r = (RadioButton)ctrl.group.getToggles().get(hour-10);
        if(myHour == -1 && myDate == "") {
            r.setDisable(false);
        }
            visits.get(date).put(hour, false);

    }

    private void updateAddVisit() {
        String date = "";
        int hour = -1;
        try {
            date = dis.readUTF();
            hour = dis.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(visits.get(date) == null){
            HashMap<Integer,Boolean> hm = new HashMap<>();
            hm.put(hour, true);
            visits.put(date, hm);
        }
        else{
            visits.get(date).put(hour, true);
        }
        RadioButton r = (RadioButton)ctrl.group.getToggles().get(hour-10);

        //dont disselect if its mine selection!!!
         if(myDate != date && myHour != hour) {
            r.setSelected(false);
            r.setDisable(true);
        }


    }
    /*
    Return is all hours in day reserved

    map: day hashmap
     */
    private Boolean isFullDay(HashMap<Integer, Boolean> map) {
        Boolean full = true;
        for(Integer i = 10; i <19; i++){
            Boolean d = map.getOrDefault(i, false);
            if(!d){
                full = false;
                break;
            }
        }
        return full;
    }
    /*
    Return String with free hours

    map: day HashMap
     */
    private String getFreeHours(HashMap<Integer, Boolean> map) {
        String s = "Wolne: ";
        for(Integer i = 10; i <19; i++){
            Boolean d = map.getOrDefault(i, false);
            if(!d){
                s += i.toString()+ ":00 ";
            }
        }
        return s;
    }
    /*
        Return is any hour in day reserved
            formattedString: day in format yyy-LL-dd
     */
    private Boolean isDayBusy(String formattedString) {
        if(!visits.containsKey(formattedString)) return false;
        for( Map.Entry<Integer, Boolean> entry : visits.get(formattedString).entrySet()){
            if(entry.getValue()) return true;
        }
        return false;
    }
}
