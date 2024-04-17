package app.backdoor;

import java.io.IOException;

import backdoor.Backdoor;

public class BackDoorMain {
    public static void main(String[] args) {
        Backdoor backdoor = new Backdoor();
        try {
            backdoor.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
