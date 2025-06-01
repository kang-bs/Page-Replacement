import javax.swing.*;
import java.awt.*;
import java.util.*;

public class PageReplacementSimulator extends JFrame {
    private JTextField refStringField;
    private JTextField frameField;
    private JButton runBtn, randomBtn;
    private JTextArea resultArea;
    private JPanel gridPanel;
    private JScrollPane scrollGridPanel;
    private FIFO fifoCore;
    private LRU lruCore;
    private LFU lfuCore;
    private Core core;
    private JComboBox<String> policyCombo;

    private final int CELL_SIZE = 40;

    public PageReplacementSimulator() {
        setTitle("Page Replacement Simulator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 520);
        setLayout(null);

        JLabel label1 = new JLabel("참조 문자열:");
        label1.setBounds(20, 20, 80, 30);
        add(label1);

        refStringField = new JTextField("70720304230032");
        refStringField.setBounds(100, 20, 200, 30);
        add(refStringField);

        JLabel label2 = new JLabel("프레임 수:");
        label2.setBounds(320, 20, 60, 30);
        add(label2);


        frameField = new JTextField("3");
        frameField.setBounds(380, 20, 40, 30);
        add(frameField);

        runBtn = new JButton("실행");
        runBtn.setBounds(440, 20, 70, 30);
        add(runBtn);

        randomBtn = new JButton("랜덤");
        randomBtn.setBounds(520, 20, 70, 30);
        add(randomBtn);

        String[] policies = {"FIFO", "LRU", "LFU"};
        policyCombo = new JComboBox<>(policies);
        policyCombo.setBounds(600, 20, 100, 30); // 520+70+10=600
        add(policyCombo);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBounds(600, 60, 310, 200);
        add(resultScroll);

        // gridPanel 생성
        gridPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
            }
        };
        gridPanel.setBackground(Color.BLACK);

        // JScrollPane에 gridPanel을 넣기
        scrollGridPanel = new JScrollPane(gridPanel);
        scrollGridPanel.setBounds(20, 70, 550, 350);
        scrollGridPanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollGridPanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(scrollGridPanel);

        runBtn.addActionListener(e -> runSimulation());
        randomBtn.addActionListener(e -> generateRandomString());

        setVisible(true);
    }

    private void runSimulation() {
        String input = refStringField.getText().trim();
        char[] chars = input.toCharArray();
        int frameSize;
        try {
            frameSize = Integer.parseInt(frameField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "프레임 수를 올바르게 입력하세요.");
            return;
        }
        if (chars.length == 0 || frameSize <= 0) {
            JOptionPane.showMessageDialog(this, "참조 문자열과 프레임 수를 올바르게 입력하세요.");
            return;
        }
        for (char c : chars) {
            if (!Character.isDigit(c)) {
                JOptionPane.showMessageDialog(this, "숫자만 입력하세요.");
                return;
            }
        }

        // Page ID 초기화
        Page.CREATE_ID = 0;

        String policy = (String) policyCombo.getSelectedItem();

        fifoCore = new FIFO(frameSize);
        lruCore = new LRU(frameSize);
        lfuCore = new LFU(frameSize);

        resultArea.setText(""); // 결과창 초기화

        // 정책에 따라 core에 할당
        if ("LRU".equals(policy)) {
            for (char ch : chars) {
                lruCore.operate(ch);
            }
            core = lruCore;
        } else if ("FIFO".equals(policy)) {
            for (char ch : chars) {
                fifoCore.operate(ch);
            }
            core = fifoCore;
        } else if("LFU".equals(policy)) {
            for (char ch : chars) {
                lfuCore.operate(ch);
            }
            core = lfuCore;
        }

        displayResult(core, chars.length);

        gridPanel.setPreferredSize(calcGridPreferredSize(core.pageHistory.size(), core.frameSize));
        gridPanel.revalidate();
        gridPanel.repaint();

    }

    private void displayResult(Core core, int refLen) {
        for (Page p : core.pageHistory) {
            String msg = String.format("DATA %c is %s",
                    p.data,
                    p.status == Page.Status.HIT ? "Hit" :
                            p.status == Page.Status.PAGEFAULT ? "Page Fault" : "Migrated");
            resultArea.append(msg + "\n");
        }
        resultArea.append("\n총 Hit: " + core.hit + "\n");
        resultArea.append("총 Fault: " + core.fault + "\n");
        resultArea.append("총 Migration: " + core.migration + "\n");
        double faultRate = (double) core.fault / refLen * 100.0;
        resultArea.append(String.format("Page Fault Rate: %.2f%%\n", faultRate));
    }

    private Dimension calcGridPreferredSize(int n, int frameSize) {
        int width = Math.max(550, n * CELL_SIZE + 20);
        int height = Math.max(350, frameSize * CELL_SIZE + 40);
        return new Dimension(width, height);
    }

    private void drawGrid(Graphics g) {
        if (core == null || core.pageHistory.isEmpty()) return;
        int n = core.pageHistory.size();
        int frameSize = core.frameSize;
        String policy = (String) policyCombo.getSelectedItem();
        boolean isLRU = "LRU".equals(policy);
        boolean isLFU = "LFU".equals(policy);

        // 헤더(참조 문자열)
        g.setColor(Color.WHITE);
        for (int i = 0; i < n; i++) {
            g.drawString(String.valueOf(core.pageHistory.get(i).data), 18 + i * CELL_SIZE, 18);
        }

        // 프레임 상태 시뮬레이션
        char[][] frameStates = new char[frameSize][n];
        int[][] pidStates = new int[frameSize][n];

        if (isLRU || isLFU) {
            // LRU: 프레임 배열을 직접 관리
            Page[] frameArr = new Page[frameSize];
            for (int t = 0; t < n; t++) {
                Page page = core.pageHistory.get(t);
                if (page.status == Page.Status.HIT) {
                    // 아무것도 안 바뀜
                } else if (page.status == Page.Status.PAGEFAULT) {
                    // 빈 공간에 추가
                    for (int i = 0; i < frameSize; i++) {
                        if (frameArr[i] == null) {
                            frameArr[i] = page;
                            break;
                        }
                    }
                } else if (page.status == Page.Status.MIGRATION && page.replacedIndex >= 0) {
                    // 교체 인덱스에만 새 페이지로 교체
                    frameArr[page.replacedIndex] = page;
                }
                // 프레임 복사
                for (int i = 0; i < frameSize; i++) {
                    if (frameArr[i] != null) {
                        frameStates[i][t] = frameArr[i].data;
                        pidStates[i][t] = frameArr[i].pid;
                    } else {
                        frameStates[i][t] = 0;
                        pidStates[i][t] = -1;
                    }
                }
            }
        } else {
            // FIFO: 큐 기반
            java.util.Queue<Page> window = new LinkedList<>();
            for (int t = 0; t < n; t++) {
                Page page = core.pageHistory.get(t);
                if (page.status == Page.Status.HIT) {
                    // 아무것도 안 바뀜
                } else {
                    if (window.size() == frameSize) window.poll();
                    window.offer(page);
                }
                int i = 0;
                for (Page p : window) {
                    frameStates[i][t] = p.data;
                    pidStates[i][t] = p.pid;
                    i++;
                }
                while (i < frameSize) {
                    frameStates[i][t] = 0;
                    pidStates[i][t] = -1;
                    i++;
                }
            }
        }

        // 하이라이트용: 각 시점별 HIT/MIGRATION/PAGEFAULT 프레임 인덱스
        int[] highlightIdx = new int[n];
        Arrays.fill(highlightIdx, -1);
        for (int t = 0; t < n; t++) {
            Page p = core.pageHistory.get(t);
            if (p.status == Page.Status.HIT) {
                for (int i = 0; i < frameSize; i++) {
                    if (frameStates[i][t] == p.data) {
                        highlightIdx[t] = i;
                        break;
                    }
                }
            } else if (isLRU && p.status == Page.Status.MIGRATION && p.replacedIndex >= 0) {
                highlightIdx[t] = p.replacedIndex;
            } else {
                for (int i = 0; i < frameSize; i++) {
                    if (frameStates[i][t] == p.data &&
                            (t == 0 || frameStates[i][t-1] != p.data)) {
                        highlightIdx[t] = i;
                        break;
                    }
                }
            }
        }

        // 그리드 그리기
        for (int f = 0; f < frameSize; f++) {
            for (int t = 0; t < n; t++) {
                int x = 10 + t * CELL_SIZE;
                int y = 30 + f * CELL_SIZE;
                g.setColor(Color.WHITE);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                char val = frameStates[f][t];
                int pid = pidStates[f][t];
                if (val != 0) {
                    // 하이라이트
                    if (highlightIdx[t] == f) {
                        Page.Status status = core.pageHistory.get(t).status;
                        if (status == Page.Status.HIT) {
                            g.setColor(Color.GREEN);
                        } else {
                            g.setColor(Color.RED);
                        }
                        g.fillRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2);
                        g.setColor(status == Page.Status.HIT ? Color.BLACK : Color.WHITE);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    // 페이지 번호
                    g.drawString(String.valueOf(val), x + CELL_SIZE / 2 - 6, y + CELL_SIZE / 2 + 6);
                }
            }
        }
    }

    private void generateRandomString() {
        Random r = new Random();
        int len = r.nextInt(10) + 10;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(r.nextInt(10));
        }
        refStringField.setText(sb.toString());
    }
}


