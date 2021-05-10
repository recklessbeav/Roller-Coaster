import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CarRide {


    private int carCapacity, numCars, nextCar, nextRequestLoad, nextUnload;
    private int[] passengers;

    private ReentrantLock carRideLock;
	private Condition load; //car load passenger
	private Condition unload; //car is unloading
	private Condition fullCar; //car is full
	private Condition loadNextPass; //load next passenger

    private boolean full = true;

    public CarRide(int c, int m) {
    
        this.carCapacity = c;
		this.numCars = m;
		this.passengers = new int[m];

        carRideLock = new ReentrantLock();
		load = carRideLock.newCondition();
		unload = carRideLock.newCondition();
		fullCar = carRideLock.newCondition();
		loadNextPass = carRideLock.newCondition();

    }

    public void ride(int id){
        carRideLock.lock();

        while (passengers[nextCar] == carCapacity) {
			System.out.println("Passenger " + id + ": Waiting to load");
			load.awaitUninterruptibly();
		}
		
		final int ridingCar = nextCar;
		
        passengers[ridingCar]++;
		System.out.println("Passenger " + id + ": Ride the car " + ridingCar);
		
        if (passengers[ridingCar] == carCapacity) {
			fullCar.signal();
		}

        //waits until the car gets full
		fullCar.awaitUninterruptibly();
		
        //waits the end of the ride
		unload.awaitUninterruptibly();
		System.out.println("Passenger " + id + ": Left the car " + ridingCar);
		
		carRideLock.unlock();
    }

    public void load(){
        carRideLock.lock();
		
		final int thisCar = nextRequestLoad;

		//index of the next car that will carry passengers
		nextRequestLoad = (nextRequestLoad + 1) % numCars;

		//waiting car:
		if (thisCar != nextCar) {
			loadNextPass.awaitUninterruptibly();
		}

		full = false;

		//car is ready to load 
		load.signalAll();
		System.out.println("Car " + thisCar + " is waiting for passengers \n");
		
        //waits until the car gets full
		fullCar.awaitUninterruptibly();
		
		full = true;
		
        //car full
		fullCar.signalAll();
        System.out.println("All aboard car " + thisCar + "\n");
		nextCar = (nextCar + 1) % numCars;
		
        //next car
		loadNextPass.signal();
		
		carRideLock.unlock();
    }


    public void unload(){
        carRideLock.lock();
		
		//Unload the passengers:
		for (int i = 0; i < carCapacity; i++) {
			unload.signal();
		}
		passengers[nextUnload] = 0;
		System.out.println("All ashore car " + nextUnload + "\n");
		nextUnload = (nextUnload + 1) % numCars;
		
		carRideLock.unlock();
    }

}
