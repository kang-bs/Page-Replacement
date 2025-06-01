import java.util.*;

public class LRU extends Core{
    private int time;
    public List<Page> frameWindow;


    public LRU(int frameSize) {
        super(frameSize);
        this.time = 0;
        this.frameWindow = new ArrayList<>();
    }

    public Page.Status operate(char data) {
        Page newPage = new Page();

        time++;
        boolean isHit = false;
        int i=0;
        int hitIndex = -1;
        for(Page p : frameWindow) {
            if(p.data == data) {
                isHit = true;
                p.lastHitTime = time;
                hitIndex = i;
                break;
            }
            i++;
        }

        if(isHit) {
            newPage.pid = Page.CREATE_ID++;
            newPage.data = data;
            newPage.status = Page.Status.HIT;
            newPage.lastHitTime = time;
            this.hit++;
            newPage.loc = hitIndex +1;
            pageHistory.add(newPage);
            newPage.replacedIndex = -1;
            return Page.Status.HIT;
        } else {
            newPage.pid = Page.CREATE_ID++;
            newPage.data = data;
            newPage.lastHitTime = time;

            if(frameWindow.size() < frameSize) {
                newPage.status = Page.Status.PAGEFAULT;
                frameWindow.add(newPage);
                this.fault++;
                newPage.replacedIndex = frameWindow.size() - 1;
            } else {
                int lruIndex = 0;
                int minTime = frameWindow.get(0).lastHitTime;
                for(int j = 1; j < frameWindow.size(); j++) {
                    if(frameWindow.get(j).lastHitTime < minTime) {
                        minTime = frameWindow.get(j).lastHitTime;
                        lruIndex = j;
                    }
                }
                frameWindow.set(lruIndex, newPage);
                newPage.status = Page.Status.MIGRATION;
                newPage.replacedIndex = lruIndex; // 교체 위치 기록
                this.migration++;
                this.fault++;
            }
            newPage.loc = frameWindow.size();
        }
        pageHistory.add(newPage);
        return newPage.status;
    }
}

