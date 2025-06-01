public class Page {
    public enum Status { HIT, PAGEFAULT, MIGRATION }
    public static int CREATE_ID = 0;

    public int pid;      // 고유 ID
    public int loc;      // 프레임 내 위치(1부터 시작)
    public char data;    // 페이지 데이터
    public Status status;

    public int lastHitTime;
    public int replacedIndex = -1;
    public int inTime = 0;
}