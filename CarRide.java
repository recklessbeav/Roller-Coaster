import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CarRide {


    private int carCapacity, numCars;
    private int[] passengers;

    private ReentrantLock rollerCoasterLock;
	private Condition carLoad; //car load passenger
	private Condition carUnload; //car is unloading
	private Condition carIsFull; //car is full
	private Condition nextCarCanLoad; //load next passenger
    
    private int nextLoad = 0; //index of the next car 
    private int nextRequestLoad = 0; //index of the next car (requesting)
    private int nextUnload = 0; //index of the next car (unload)


    private boolean full = true;

    public CarRide(int c, int m) {
    
        this.carCapacity = c;
		this.numCars = m;
		this.passengers = new int[m];

        rollerCoasterLock = new ReentrantLock();
		carLoad = rollerCoasterLock.newCondition();
		carUnload = rollerCoasterLock.newCondition();
		carIsFull = rollerCoasterLock.newCondition();
		nextCarCanLoad = rollerCoasterLock.newCondition();

    }

    public void ride(int id){
        rollerCoasterLock.lock();

        while (passengers[nextLoad] == carCapacity) {
			System.out.println("Passenger " + id + ": Waiting to load");
			carLoad.awaitUninterruptibly();
		}
		
		final int ridingCar = nextLoad;
		
        passengers[ridingCar]++;
		System.out.println("Passenger " + id + ": Ride the car " + ridingCar);
		
        if (passengers[ridingCar] == carCapacity) {
			carIsFull.signal();
		}

		
        //waits until the car gets full
		carIsFull.awaitUninterruptibly();
		
        //waits the end of the ride
		carUnload.awaitUninterruptibly();
		System.out.println("Passenger " + id + ": Left the car " + ridingCar);
		
		rollerCoasterLock.unlock();
    }

    public void load(){
        rollerCoasterLock.lock();
		
		final int thisCar = nextRequestLoad;

		//index of the next car that will carry passengers
		nextRequestLoad = (nextRequestLoad + 1) % numCars;

		//waiting car:
		if (thisCar != nextLoad) {
			nextCarCanLoad.awaitUninterruptibly();
		}

		full = false;

		//car is ready to load 
		carLoad.signalAll();
		System.out.println("Car " + thisCar + ": Waiting for passengers");
		
        //waits until the car gets full
		carIsFull.awaitUninterruptibly();
		full = true;
		
        //car full
		carIsFull.signalAll();
        System.out.println("All aboard car " + thisCar + "\n");
		nextLoad = (nextLoad + 1) % numCars;
		
        //next car
		nextCarCanLoad.signal();
		
		rollerCoasterLock.unlock();
    }


    public void unload(){
        rollerCoasterLock.lock();
		
		//Unload the passengers:
		for (int i = 0; i < carCapacity; i++) {
			carUnload.signal();
		}
		passengers[nextUnload] = 0;
		System.out.println("All ashore car " + nextUnload + "\n");
		nextUnload = (nextUnload + 1) % numCars;
		
		rollerCoasterLock.unlock();
    }

}
