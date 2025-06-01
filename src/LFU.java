import java.util.*;

public class LFU extends Core{
    public List<Page> frameWindow;
    private Map<Character, Integer> refCount;
    private int time;

    public LFU(int frameSize) {
        super(frameSize);
        this.time = 0;
        this.refCount = new HashMap<>();
        this.frameWindow = new ArrayList<>();
    }

    @Override
    public Page.Status operate(char data){
        Page newPage = new Page();

        boolean isHit = false;
        time++;
        int i= 0;
        int hitIndex = -1;
        for(Page p : frameWindow){
            if(p.data == data){
                isHit = true;
                hitIndex = i;
                break;
            }
            i++;
        }

        if(isHit){
            refCount.put(data, refCount.get(data) + 1);
            newPage.pid = Page.CREATE_ID++;
            newPage.data = data;
            newPage.status = Page.Status.HIT;
            this.hit++;
            newPage.loc = hitIndex + 1;
            newPage.replacedIndex = -1;
            pageHistory.add(newPage);
            return Page.Status.HIT;
        } else {
            newPage.pid = Page.CREATE_ID++;
            newPage.data = data;
            newPage.inTime = time;
            refCount.put(data, 1);

            if(frameWindow.size()<frameSize){
                newPage.status = Page.Status.PAGEFAULT;
                frameWindow.add(newPage);
                newPage.replacedIndex = frameWindow.size()-1;
                this.fault++;
            } else {
                int minCount = Integer.MAX_VALUE;
                for(Page p : frameWindow){
                    int count = refCount.get(p.data);
                    if(count < minCount){
                        minCount = count;
                    }
                }

                // 2. 동률 중 가장 오래된(작은 insertTime) 찾기
                int lfuIndex = -1;
                int oldestTime = Integer.MAX_VALUE;
                for (int j = 0; j < frameWindow.size(); j++) {
                    Page p = frameWindow.get(j);
                    if (refCount.get(p.data) == minCount) {
                        if (p.inTime < oldestTime) {
                            oldestTime = p.inTime;
                            lfuIndex = j;
                        }
                    }
                }

                Page removed = frameWindow.remove(lfuIndex);
                refCount.remove(removed.data);
                frameWindow.add(lfuIndex,newPage);
                newPage.status = Page.Status.MIGRATION;
                newPage.replacedIndex = lfuIndex;
                this.migration++;
                this.fault++;

            }
            newPage.loc = frameWindow.size();
            pageHistory.add(newPage);
            return newPage.status;
        }

    }

}

