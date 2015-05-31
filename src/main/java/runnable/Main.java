package runnable;

import setup.DatabaseController;

/**
 * This class is used to...
 * Created by Patrick van de Graaf
 */
public class Main {
    public static void main(String [] args){
        DatabaseController.getInstance().setup();
    }
}
