import java.util.*;

public class Core {
    public int frameSize;
    public List<Page> pageHistory;
    public int hit, fault, migration;

    public Core(int frameSize){
        this.frameSize = frameSize;
        this.pageHistory = new ArrayList<>();
    }

    public Page.Status operate(char data){
        return Page.Status.HIT;
    }
}
