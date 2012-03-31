import java.util.*;
import java.io.*;
class Start{
    public static int port, numberOfAccesses, numberOfReaders, numberOfWriters;
    public static long startTime;
    public static int[] readerOpTimes, writerOpTimes, readerSleepTimes, writerSleepTimes;
    public static String[] readers, writers;
    public static String serverName;

    //parse system.properties file
    private static final String PROP_FILE="system.properties";  
    public static void readPropertiesFile(){  
        try{
            InputStream is = Start.class.getResourceAsStream(PROP_FILE);  
            Properties prop = new Properties();  
            prop.load(is);  
            port = Integer.parseInt(prop.getProperty("Rmiregistry.port"));  
            serverName = prop.getProperty("RW.server");
	    numberOfAccesses = Integer.parseInt(prop.getProperty("RW.numberOfAccesses").trim());	   
	    numberOfReaders = Integer.parseInt(prop.getProperty("RW.numberOfReaders").trim());	   
            readers = new String[numberOfReaders+1];
            for(int i =1;i<=numberOfReaders;i++){
	 	    String readerName = "RW.reader" + i;		
		    readers[i] = (prop.getProperty(readerName)).trim();	   
	    }
		
	    numberOfWriters = Integer.parseInt(prop.getProperty("RW.numberOfWriters").trim());	   
	    writers = new String[numberOfWriters + 1];
	    for(int i =numberOfReaders+1;i<=numberOfReaders+numberOfWriters;i++){
		    String writerName = "RW.writer" + i;		
		    writers[i-numberOfReaders] = (prop.getProperty(writerName)).trim();	   
	    }

	    readerOpTimes = new int[numberOfReaders+1];
	    readerSleepTimes = new int[numberOfReaders+1];
	    writerOpTimes  = new int[numberOfWriters+1];
	    writerSleepTimes = new int[numberOfWriters+1];

            //scan opTimes and sleepTimes
            for(int i =1;i<=numberOfReaders;i++){
		    readerOpTimes[i] = Integer.parseInt((prop.getProperty("RW.reader" + i + ".opTime")).trim());	   
		    readerSleepTimes[i] = Integer.parseInt((prop.getProperty("RW.reader" + i + ".sleepTime")).trim());	   
	    }

	    for(int i =numberOfReaders+1;i<=numberOfReaders+numberOfWriters;i++){
		    writerOpTimes[i-numberOfReaders] = Integer.parseInt((prop.getProperty("RW.writer" + i + ".opTime")).trim());	   
		    writerSleepTimes[i-numberOfReaders] = Integer.parseInt((prop.getProperty("RW.writer" + i + ".sleepTime")).trim());	   
	    }
            
            is.close();
        }catch(Exception e){  
            System.out.println("Failed to read from " + PROP_FILE + " file." + e);  
        }
    }

    public static void main(String[] args){
	    readPropertiesFile();
	    try{
		    Runtime.getRuntime().exec("rmic Hello");
		    Thread.sleep(3000);
	    }catch(Exception e){ }

	    String path = System.getProperty("user.dir");
	    long startTime = System.currentTimeMillis();
	    try{
		    //start server
		    String t = "ssh -o StrictHostKeyChecking=no " + serverName + " cd " + path + "; java Server " + startTime + " " + numberOfAccesses + " " + port + " " + numberOfReaders + " " + numberOfWriters + " " + serverName;
		    System.out.println(t);
		    Runtime.getRuntime().exec(t);
	
	  	    //delay
		   // Thread.sleep(2000);

		    //start clients
		    int type ; //0 :reader, 1:writer
		    type = 0 ;
		    for(int i = 1;i<= numberOfReaders;i++){
			    t = "ssh -o StrictHostKeyChecking=no " + readers[i]   +  " cd " + path + "; java Client " + (i) + " "+ type + " "+ readerSleepTimes[i] + " " + readerOpTimes[i] + " " + startTime;
			    System.out.println(t);
			    Runtime.getRuntime().exec(t);
		    }
		    type = 1;
		    for(int i = 1;i<= numberOfWriters;i++){
			    t = "ssh -o StrictHostKeyChecking=no " + writers[i]   +  " cd " + path + "; java Client " + (i+numberOfReaders) + " " + type + " " + writerSleepTimes[i]+ " "+ writerOpTimes[i] + " " + startTime ;
			    System.out.println(t);
			    Runtime.getRuntime().exec(t);
		    }


	    }catch(Exception e){
		    System.out.println(e);
	    }
    }
}
