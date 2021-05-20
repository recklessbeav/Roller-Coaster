import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

public class MulticarRollerCoaster {
	
	protected final int CAR_CAPACITY;
	protected final int NUM_CARS;
    protected final int NUM_PASSENGERS;
	
	private final int[] passengersInCar; //Array whose values are the number of passengers in the car[i]
	private int nextCarRequestLoad = 0; //Index of the next car that will request load
	private int nextCarToUnload = 0; //Index of the next car that will be unloaded
	private int nextCarToLoad = 0; //Index of the next car that will load
	private boolean nextCarToLoadIsWaitingPassengers = false; //True if the next car that will load is waiting for passengers
	
	private final ReentrantLock rollerCoasterLock;
	private final Condition carLoad; //Signaled when a car can load passengers
	private final Condition carUnload; //Signaled when a car is unloading its passengers
	private final Condition carIsFull; //Signaled when a car is full
	private final Condition nextCarCanLoad; //Signaled when the next car to load can load passengers

    private final ArrayList<Integer> passengerStatus;
    int psuccess=0;
    int pwaiting=0;
	
	/**
	 * Creates a Roller Coaster implementation for several cars using monitor.
	 * @param carCapacity number of passengers per car
	 * @param numCars number of cars
	 */
	public MulticarRollerCoaster(int carCapacity, int numCars, int numPassengers) {
		this.CAR_CAPACITY = carCapacity;
		this.NUM_CARS = numCars;
        this.NUM_PASSENGERS = numPassengers;
		this.passengersInCar = new int[numCars]; //Values implicitly initialized to zero
        this.passengerStatus = new ArrayList<Integer>(numCars+1);
		
		rollerCoasterLock = new ReentrantLock();
		carLoad = rollerCoasterLock.newCondition();
		carUnload = rollerCoasterLock.newCondition();
		carIsFull = rollerCoasterLock.newCondition();
		nextCarCanLoad = rollerCoasterLock.newCondition();
	}
	
	/**
	 * Requests to take ride in a car.
	 * @param passengerId identifier of the passenger
	 */
	public void takeRide(final int passengerId) {
        rollerCoasterLock.lock();
        boolean checker = passengerStatus.contains(passengerId);
        if(!checker){
            
       
            //Waits for an available car:
            while (!nextCarToLoadIsWaitingPassengers || passengersInCar[nextCarToLoad] == CAR_CAPACITY) {
                System.out.println("[Passenger " + (passengerId+1) + "] is currently waiting for an available car...");
                carLoad.awaitUninterruptibly();
            }
            
            final int ridingCar = nextCarToLoad;
            passengersInCar[ridingCar]++;
            System.out.println("[Passenger " + (passengerId+1) + "] boards Car " + (ridingCar+1));
            pwaiting++;
            
            if (passengersInCar[ridingCar] == CAR_CAPACITY) {
                //Notifies the car that it is full:
                carIsFull.signal();
            }		
            //Waits until the car gets full:
            carIsFull.awaitUninterruptibly();
            //Waits the end of the ride:
            carUnload.awaitUninterruptibly();
            System.out.println("[Passenger " + (passengerId+1) + "] unboards Car " + (ridingCar+1));
            passengerStatus.add(passengerId);
            psuccess++;
            //System.out.println("success: " + psuccess);
            
        }

        //System.out.println(" waiting: " + pwaiting + " res: " + NUM_PASSENGERS % psuccess);
        if(psuccess == NUM_PASSENGERS){
            System.out.println("\nAll rides completed!");
            System.exit(0);
        } else if(pwaiting == (NUM_PASSENGERS % psuccess) && psuccess == (NUM_PASSENGERS - (NUM_PASSENGERS % CAR_CAPACITY))){
           // System.out.print("success: " + psuccess);
            //System.out.println(" waiting: " + pwaiting + " res: " + NUM_PASSENGERS % psuccess);
            System.out.println("\n" + pwaiting + " passenger/s is/are not able to successfully ride the roller coaster. All rides completed!");
            System.exit(0);
        }
        rollerCoasterLock.unlock();
	}
	
	/**
	 * Requests a car to carry passengers.
	 */
	public void load() {
		rollerCoasterLock.lock();
		
		final int thisCar = nextCarRequestLoad;
		//Updates the index of the next car that will carry passengers:
		nextCarRequestLoad = (nextCarRequestLoad + 1) % NUM_CARS;
		//If this car is not the next car to go around then waits its successor car to leave:
		if (thisCar != nextCarToLoad) {
			nextCarCanLoad.awaitUninterruptibly();
		}
		nextCarToLoadIsWaitingPassengers = true;
		//Notifies the passengers the car is ready to load them:
		carLoad.signalAll();
		System.out.println("[Car " + (thisCar+1) + "] is waiting for passengers...");
		//Waits until the car gets full:
		carIsFull.awaitUninterruptibly();
		nextCarToLoadIsWaitingPassengers = false;
		//Notifies the passengers that the car is full:
		carIsFull.signalAll();
		System.out.println("All aboard Car " + (thisCar+1) + "! Enjoy the ride :)\n");
        //running_cars[thisCar] = true;
		nextCarToLoad = (nextCarToLoad + 1) % NUM_CARS;
		//Awakes up the next car, i.e. its predecessor: 
		nextCarCanLoad.signal();
		
		rollerCoasterLock.unlock();
	}
	
	/**
	 * Unload the passengers from the arriving car.
	 */
	public void unload() {
		rollerCoasterLock.lock();
		
		//Unload the passengers:
		for (int i = 0; i < CAR_CAPACITY; i++) {
			carUnload.signal();
            pwaiting--;
		}
		passengersInCar[nextCarToUnload] = 0;
		System.out.println("All ashore from Car " + (nextCarToUnload+1) + "! Thank you for riding with us :)");
        // running_cars[nextCarToUnload] = false;
        // System.out.println("run stat: " + running_cars[nextCarToUnload]);
		nextCarToUnload = (nextCarToUnload + 1) % NUM_CARS;
		
		rollerCoasterLock.unlock();
	}
}