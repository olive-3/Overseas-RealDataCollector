package stock.overseas.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;

public class MyFrame extends JFrame {

    private final String newline = "\n";
    private final JTextArea textArea = new JTextArea();
    private final LineBorder border = new LineBorder(Color.LIGHT_GRAY, 1, true);
    private static final MyFrame myFrame = new MyFrame();

    public static MyFrame getInstance() {
        return myFrame;
    }

    private MyFrame() {
        JLabel description = new JLabel("실시간 데이터 수집 정보를 출력합니다.");
        description.setBounds(50,35,350,30);

        textArea.setBounds(50, 70, 900, 350);
        textArea.setBorder(border);
        textArea.setEditable(false);

        add(description); add(textArea);
        setTitle("RealDataCollector");
        setSize(1000,500);
        setLayout(null);
        setVisible(true);
    }

    public void actionPerformed(LocalDateTime datetime, String content) {
        textArea.append("[" + datetime.toString() + "]" + content + newline);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

}
