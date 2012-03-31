import java.rmi.*;

public interface HelloInterface extends Remote {
    public String say(int id,int type, int sleepTime ,int opTime) throws RemoteException;
}
