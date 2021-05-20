//reference: https://github.com/rafaelsales/ConcurrentProgramming/tree/master/ConcurrentJavaRollerCoaster/src/rafael/concurrent/monitor/rollercoaster

import java.util.Scanner;

public class Main {
	public static void main(String[] args) {
		int numCars;
		int carCapacity;
		int numPassengers;
		Scanner scannerInt = new Scanner(System.in);
		do {
			System.out.print("Number of cars: ");
			numCars = scannerInt.nextInt();
		} while (numCars <= 0);
		do {
			System.out.print("Number of passengers per car: ");
			carCapacity = scannerInt.nextInt();
		} while (carCapacity <= 0);
		do {
			System.out.print("Number of passengers: ");
			numPassengers = scannerInt.nextInt();
		} while (numPassengers <= 0);
		System.out.println();
		
		final MulticarRollerCoaster rollerCoaster = new MulticarRollerCoaster(carCapacity, numCars, numPassengers);

		for (int i = 0; i < numCars; i++) {
			Thread carThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						rollerCoaster.load();
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
						rollerCoaster.unload();
					}
				}
			}, "Car " + i);
			carThread.start();
		}
		
		for (int i = 0; i < numPassengers; i++) {
			final int passengerId = i;
			Thread passengerThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						rollerCoaster.takeRide(passengerId);
					}
				}
			}, "Passenger " + passengerId);
			passengerThread.start();
		}
	}
}