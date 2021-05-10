
import java.util.concurrent.locks.*;
import java.util.*;
import java.lang.*;

public class Main {
    public static void main(String args[])  {  
        Scanner sc = new Scanner(System.in);    
        
        System.out.print("Enter the number of processes: ");  
        int n = sc.nextInt();  
        System.out.print("Enter the capacity of the car: ");  
        int C = sc.nextInt();  
        System.out.print("Enter the number of cars: ");  
        int m = sc.nextInt();  
     	

        Random rand = new Random();
	    int wanderLength = rand.nextInt(50) + 1;;	        

        CarRide cars = new CarRide(C,m);
            
        for (int i = 0; i < m; i++) {
            Thread carThread = new Thread(new Runnable() {
            @Override
                public void run() {
                    while (true) {
                        cars.load();
                        
                        try {
                            Thread.sleep(wanderLength);
                        } catch (InterruptedException e) {
                        }
                    
                        cars.unload();
                    }
                }
            }, "Car " + i);
            
            carThread.start();
        }


        for (int i = 0; i < C; i++) {
            
            int id = i;
            
            Thread passengerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        cars.ride(id);
                    }
                }
            }, "Passenger " + id);
            passengerThread.start();
        }


    }  
}
