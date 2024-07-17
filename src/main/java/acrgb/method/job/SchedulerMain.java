/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package acrgb.method.job;

import java.util.Timer;
//import static javax.management.Query.times;

/**
 *
 * @author MinoSun
 */
public class SchedulerMain {
//

    public String RunMethod() {
        String result = "";
        try {
            Timer t = new Timer(); // Instantiate Timer Object
            QuartzJob st = new QuartzJob(); // Instantiate SheduledTask class
            t.schedule(st, 0, 1000); // Create Repetitively task for every 1 secs
//
//		//for demo only.
            for (int i = 0; i <= 5; i++) {
                System.out.println("Execution in Main Thread...." + i);
                Thread.sleep(2000);
                if (i == 5) {
                    System.out.println("Application Terminates");
                    System.exit(0);
                }
            }
            result = "SUCCESS";
        } catch (InterruptedException ex) {
            result = ex.getLocalizedMessage();

        }
        return result;

    }

}
