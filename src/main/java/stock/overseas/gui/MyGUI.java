package stock.overseas.gui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;

public class MyGUI extends JFrame {

    private final String newline = "\n";
    private final JTextArea textArea = new JTextArea();
    private final LineBorder border = new LineBorder(Color.LIGHT_GRAY, 1, true);
    private final JButton closeButton = new JButton("프로그램 종료");
    private static final MyGUI MY_GUI = new MyGUI();

    public static MyGUI getInstance() {
        return MY_GUI;
    }

    private MyGUI() {
        JLabel description = new JLabel("실시간 데이터 수집 정보를 출력합니다.");
        description.setBounds(50,35,350,30);
        closeButton.setBounds(860, 35, 100, 30);

        textArea.setBounds(50, 70, 900, 350);
        textArea.setBorder(border);
        textArea.setEditable(false);

        add(description); add(closeButton); add(textArea);
        setTitle("RealDataCollector");
        setSize(1000,500);
        setLayout(null);
        setVisible(true);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    public void exceptionHandling(LocalDateTime datetime, String content) {
        textArea.append("[" + datetime.toString() + "] " + content + newline);
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }

}
