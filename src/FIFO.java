import java.util.*;

public class FIFO extends Core{
    public Queue<Page> frameWindow;
    private int cursor;

    public FIFO(int frameSize) {
        super(frameSize);
        this.frameWindow = new LinkedList<>();
    }

    @Override
    public Page.Status operate(char data) {
        Page newPage = new Page();

        // 이미 프레임에 있으면 HIT
        boolean isHit = false;
        int i = 0;
        for (Page p : frameWindow) {
            if (p.data == data) {
                isHit = true;
                break;
            }
            i++;
        }

        if (isHit) {
            newPage.pid = Page.CREATE_ID++;
            newPage.data = data;
            newPage.status = Page.Status.HIT;
            this.hit++;
            newPage.loc = i + 1;
        } else {
            newPage.pid = Page.CREATE_ID++;
            newPage.data = data;

            if (frameWindow.size() >= frameSize) {
                newPage.status = Page.Status.MIGRATION;
                frameWindow.poll();
                cursor = frameSize;
                this.migration++;
                this.fault++;
            } else {
                newPage.status = Page.Status.PAGEFAULT;
                cursor++;
                this.fault++;
            }

            newPage.loc = cursor;
            frameWindow.offer(newPage);
        }
        pageHistory.add(newPage);

        return newPage.status;
    }

    public List<Page> getPageInfo(Page.Status status) {
        List<Page> pages = new ArrayList<>();
        for (Page page : pageHistory) {
            if (page.status == status) {
                pages.add(page);
            }
        }
        return pages;
    }
}