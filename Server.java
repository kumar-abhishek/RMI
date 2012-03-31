import java.rmi.Naming;
import java.rmi.registry.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;
import java.io.*;

public class Server 
{
        public static void writeToFile(String s){
                // Stream to write file
                FileOutputStream fout;
                try{
                        String fileName = "IamServer";
                        fout = new FileOutputStream (fileName,true);
                        new PrintStream(fout).println(s);
                        fout.close();
                }
                catch (IOException e){
                        System.err.println ("Unable to write to file");
                        System.exit(-1);
                }
        }

	public static void main (String[] argv) 
	{
		try {
			long startTime = Long.parseLong(argv[0]);
			int numberOfAccesses = Integer.parseInt(argv[1]);
			int port = Integer.parseInt(argv[2]);
			int numReaders = Integer.parseInt(argv[3]);
			int numWriters = Integer.parseInt(argv[4]);
			String server = argv[5];

/*
			Registry registry = LocateRegistry.createRegistry(port);
			Runtime.getRuntime().exec("rmiregistry 2222");
			registry.rebind("//thunder.cise.ufl.edu/kabhishe", new Hello (startTime, numberOfAccesses));
*/
			writeToFile("in server");
			Runtime.getRuntime().exec("rmiregistry " + port);
			//Naming.rebind ("//thunder.cise.ufl.edu:2222/kabhishe", new Hello (startTime, numberOfAccesses, port, numReaders, numWriters));
			Naming.rebind ("//" + server+ ":" + port + "/kabhishe", new Hello (startTime, numberOfAccesses, port, numReaders, numWriters));
			//Hello h = new Hello(startTime, numberOfAccesses, port, numReaders, numWriters);
			writeToFile ("Server is connected and ready for operation." + System.currentTimeMillis());
			

		} 
		catch (Exception e) {
			writeToFile("in server: not connnected"+e);
		}
	}
}
