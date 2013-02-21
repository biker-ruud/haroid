package nl.haroid;

import nl.haroid.webclient.HaringHnImpl;
import org.apache.log4j.Appender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.ErrorHandler;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author ruud
 */
public class WebsiteTester extends JFrame {

    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JButton resetButton;
    private JButton connectButton;
    private JTextArea resultatenArea;


    public WebsiteTester() {
        super("Haroid Website tester.");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(getInputPanel(), BorderLayout.NORTH);
        getContentPane().add(getOutputPanel(), BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        WebsiteTester tester = new WebsiteTester();
//        tester.pack();
        tester.setSize(400, 400);
        tester.setVisible(true);
    }

    private JScrollPane getOutputPanel() {
        resultatenArea = new JTextArea();
        resultatenArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultatenArea);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JTextAreaLogger appender = new JTextAreaLogger(resultatenArea);
        Logger.getRootLogger().addAppender(appender);
        return scrollPane;
    }

    private JPanel getInputPanel() {
        JPanel inputPanel = new JPanel();
        usernameTextField = new JTextField();
        passwordTextField = new JPasswordField();
        resetButton = new JButton("reset");
        connectButton = new JButton("connect");

        inputPanel.setLayout(new GridLayout(3, 2));
        inputPanel.add(new JLabel("Username"));
        inputPanel.add(usernameTextField);
        inputPanel.add(new JLabel("Password"));
        inputPanel.add(passwordTextField);
        inputPanel.add(resetButton);
        inputPanel.add(connectButton);

        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                usernameTextField.setText("");
                passwordTextField.setText("");
            }
        });

        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                resultatenArea.setText("");
                resultatenArea.append("Verbinden met website van Hollandse Nieuwe met gebruikersnaam: " + usernameTextField.getText());
                resultatenArea.append("\n");
                HaringHnImpl haringHn = new HaringHnImpl();
                haringHn.start(usernameTextField.getText(), new String(passwordTextField.getPassword()));
            }
        });
        return inputPanel;
    }

    private class JTextAreaLogger implements Appender {

        private JTextArea textArea;

        JTextAreaLogger(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void addFilter(Filter newFilter) {
        }

        @Override
        public Filter getFilter() {
            return null;
        }

        @Override
        public void clearFilters() {
        }

        @Override
        public void close() {
        }

        @Override
        public void doAppend(LoggingEvent event) {
            textArea.append(event.getMessage().toString());
            textArea.append("\n");
        }

        @Override
        public String getName() {
            return "JTextArea Appender";
        }

        @Override
        public void setErrorHandler(ErrorHandler errorHandler) {
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Override
        public void setLayout(Layout layout) {
        }

        @Override
        public Layout getLayout() {
            return null;
        }

        @Override
        public void setName(String name) {
        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
    }
}
