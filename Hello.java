import java.rmi.*;
import java.util.*;
import java.rmi.server.*;
import java.rmi.registry.*;
import java.rmi.registry.LocateRegistry;
import java.io.*;
class Pair{
	public int id;
	public int priority;
	Pair(int id, int pr){
		this.id = id;
		this.priority = pr;
	}
}
public class Hello extends UnicastRemoteObject implements HelloInterface {
	public synchronized static void writeToFile(String s){
		// Stream to write file
		FileOutputStream fout;
		try{
			String fileName = "crew.log";
			fout = new FileOutputStream (fileName,true);
			new PrintStream(fout).println(s);
			fout.flush();
			fout.close();
		}
		catch (IOException e){
			System.err.println ("Unable to write to file");
			System.exit(-1);
		}
	}


	public void shutDown() throws RemoteException {
		try{
			//Registry registry = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
			//Registry registry = LocateRegistry.getRegistry(2222);
			//registry.unbind("//thunder.cise.ufl.edu/kabhishe");
			//registry.unbind(this.serviceName);
			//UnicastRemoteObject.unexportObject(this, true);
			System.exit(0);
		}catch(Exception e){
			//writeToFile("cant unbind" + e.getMessage());
		}
	}

	private static int ServiceSequence, ObjectValue ;
	private int numberOfAccesses, numberOfReaders, numberOfWriters;
	private String message;
	private long startTime;
	public static Boolean isCriticalSectionEmpty = true;
	public static ArrayList<Pair> priorityArrReaders = new ArrayList<Pair>();
	public static ArrayList<Integer> priorityArrWriters = new ArrayList<Integer>();
	public Hello (long sTime, int numAccesses, int port, int numReaders, int numWriters) throws RemoteException {
		//	writeToFile("" + sTime);
		startTime = sTime;
		numberOfAccesses = numAccesses;
		ServiceSequence = 1;
		ObjectValue = -1;
		numberOfReaders = numReaders;
		numberOfWriters = numWriters;
		try{ 
		//	Registry registry = LocateRegistry.createRegistry(port);
			//registry.rebind("//thunder.cise.ufl.edu/kabhishe", this);
		}catch(Exception e){
			writeToFile("exception while binding " + e);
		}

	}
	public String say(int id, int type, int sleepTime, int opTime) throws RemoteException {
		for(int num = 1; num <= numberOfAccesses ; num++)
		{
			synchronized(this){
			//	writeToFile("My id : " + id + "type: " + type + " " + System.currentTimeMillis() + "starttime: " + startTime);
			}

			try{
				Thread.sleep(sleepTime);
			}
			catch(Exception e){
				//writeToFile("in Hello.java: expcetion 1" );
				System.out.println(e);
			}

			//writeToFile("checking for cs My id : " + id + "type: " + type);

			Boolean canGetIntoCS = false;
			//check whether its a reader's request or writer's request: GET its ID in parameter as well
			//type : 0:reader, 1:writer
			//if a reader' s request: append it in to priority array
			synchronized(this){
				if(type == 0){//reader
					priorityArrReaders.add(new Pair(id,1));
				}
				else //writer
					priorityArrWriters.add(id);
			}
			//writeToFile("checking for cs 2222 My id : " + id + "type: " + type);

			synchronized(this) {
				while(canGetIntoCS == false){
					//writeToFile("checking for cs 3333 My id : " + id + "type: " + type);
					if(type == 0){ //reader
						//if in 1st 3 in array or no writer in array 
						if(priorityArrWriters.isEmpty()){
							canGetIntoCS = true;
							break;
						}
						int pos = -1;
						for(int i =0;i<priorityArrReaders.size();i++){
							if(priorityArrReaders.get(i).id == id){
								pos = i;
								break;
							}
						}
						if(pos > 2 || pos == -1){
							try{
								wait();
							}
							catch(Exception e){ } 
							continue;
						}

						if(priorityArrReaders.get(0).priority > 3){
							canGetIntoCS = true;
							break;
						}
						try { 
							wait();
						}
						catch ( Exception e){ } 
						continue;
					}
					else{ //writer
						if (isCriticalSectionEmpty == false)
							try{
								wait();
								continue;
							}
						catch(Exception e){ } 


						int pos = priorityArrWriters.indexOf(new Integer(id));
						if(pos != 0){
							try{
								wait();
								continue;
							}
							catch(Exception e){  }
						}

						if(!priorityArrReaders.isEmpty() && priorityArrReaders.get(0).priority > 3){ 
							try{
								wait();
								continue;
							}
							catch(Exception e) { }
						}
						else {
							canGetIntoCS = true;
							break;
						}	 
					}
				}
			}
			//enter into CS and at the end of it notifyAll
			//if(canGetIntoCS == true)
			{
				//writeToFile("entered cs My id : " + id + "type: " + type);

				//whoever goes next into CS:
				synchronized(this){
					isCriticalSectionEmpty = false;
				}
				String AccessedBy = "invalidValue" ;
				if(type == 0)
					AccessedBy = "R" + id;
				else if(type == 1) {
					AccessedBy = "W" + id;
					ObjectValue = id;
				}
				else {
					//writeToFile("in valid vale of nextClientType");
				}
				try{
					long start = System.currentTimeMillis() - startTime;
					Thread.sleep(opTime);
					long finish = System.currentTimeMillis() - startTime;
					synchronized(this){
						writeToFile(ServiceSequence + "\t" + ObjectValue + "\t" +AccessedBy+ "\t" + start + "-" + finish);
						++ServiceSequence; 
					}
				}
				catch(Exception e){
					//writeToFile("in Hello.java: expcetion 1: " + e  );
					System.out.println(e);
				}

				//done executing in CS:remove from array 
				if(type == 0 && !priorityArrReaders.isEmpty()){
					int removeId = -1;
					for(int i =0;i<priorityArrReaders.size();i++)
						if(priorityArrReaders.get(i).id == id){
							removeId = i;
							break;
						}
					if(removeId != -1){
						synchronized(this){
							priorityArrReaders.remove(removeId);
						}
					}
					else
						System.out.println("ERROR: REMOVE ID  NOT FOUND");
				}
				else if(type == 1 && !priorityArrWriters.isEmpty())
					synchronized(this){
						priorityArrWriters.remove(0);
					}
				synchronized(this){
					isCriticalSectionEmpty = true;
					notifyAll();
					//writeToFile("notified by : " + id + "type: " + type );
				}
			}
		}

		if(numberOfAccesses * (numberOfReaders + numberOfWriters) <= ServiceSequence ){
			shutDown();
		}
		return message;
	}
}
