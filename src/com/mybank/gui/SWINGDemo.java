package com.mybank.gui;

import com.mybank.data.DataSource;
import com.mybank.domain.Bank;
import com.mybank.domain.CheckingAccount;
import com.mybank.domain.Customer;
import com.mybank.domain.SavingsAccount;
import com.mybank.reporting.CustomerReport;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URLDecoder;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class SWINGDemo {

    private final JEditorPane log;
    private final JEditorPane reportLog;
    private final JButton show;
    private final JButton report;
    private final JComboBox clients;

    public SWINGDemo() {
        log = new JEditorPane("text/html", "");
        reportLog = new JEditorPane("text/html", "");

        log.setPreferredSize(new Dimension(500, 300));
        log.setEditable(false);

        reportLog.setPreferredSize(new Dimension(500, 200));
        reportLog.setEditable(false);

        show = new JButton("Show");
        report = new JButton("Report");
        clients = new JComboBox();
        for (int i = 0; i < Bank.getNumberOfCustomers(); i++) {
            clients.addItem(Bank.getCustomer(i).getLastName() + ", " + Bank.getCustomer(i).getFirstName());
        }
    }

    private void launchFrame() {
        JFrame frame = new JFrame("MyBank clients");
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 3));
        topPanel.add(clients);
        topPanel.add(show);
        topPanel.add(report);
        frame.add(topPanel, BorderLayout.NORTH);

        JScrollPane logScrollPane = new JScrollPane(log);
        JScrollPane reportLogScrollPane = new JScrollPane(reportLog);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, logScrollPane, reportLogScrollPane);
        splitPane.setDividerLocation(0.6);
        splitPane.setResizeWeight(0.5);
        splitPane.setEnabled(false);
        
        frame.add(splitPane, BorderLayout.CENTER);

        show.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (clients.getSelectedIndex() < 0) {
                    JOptionPane.showMessageDialog(frame, "Пожалуйста, выберите клиента.", "Ошибка", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                Customer current = Bank.getCustomer(clients.getSelectedIndex());
                StringBuilder custInfo = new StringBuilder();
                custInfo.append("<html><head><style>body { font-family: sans-serif; }</style></head><body>");
                custInfo.append("<br>&nbsp;<b><span style=\"font-size:1.5em;\">")
                        .append(current.getLastName()).append(", ")
                        .append(current.getFirstName()).append("</span></b><br><hr>");

                if (current.getNumberOfAccounts() == 0) {
                    custInfo.append("&nbsp;У клиента нет счетов.<br>");
                } else {
                    for (int i = 0; i < current.getNumberOfAccounts(); i++) {
                        com.mybank.domain.Account account = current.getAccount(i);
                        String accType = "";
                        if (account instanceof CheckingAccount) {
                            accType = "Checking";
                        } else if (account instanceof SavingsAccount) {
                            accType = "Savings";
                        }
                        custInfo.append("&nbsp;<b>Тип счета: </b>").append(accType)
                                .append("<br>&nbsp;<b>Баланс: <span style=\"color:red;\">$")
                                .append(String.format("%,.2f", account.getBalance()))
                                .append("</span></b><br><br>");
                    }
                }
                custInfo.append("</body></html>");
                log.setText(custInfo.toString());
            }
        });

        report.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CustomerReport customerReport = new CustomerReport();

                PrintStream originalOut = System.out;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream newOut = new PrintStream(baos);
                System.setOut(newOut);

                try {
                    customerReport.generateReport();

                    reportLog.setText("<html><pre style=\"font-family: monospace; font-size: 1.0em;\">" + baos.toString() + "</pre></html>");
                } finally {
                    System.setOut(originalOut);
                }
            }
        });

        frame.setSize(new Dimension(550, 600));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(true);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            File currentClassFile = new File(URLDecoder.decode(SWINGDemo.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath(), "UTF-8"));
            String classFileDirectory = currentClassFile.getParent();

            DataSource data = new DataSource("data/test.dat");
            data.loadData();

        } catch (IOException ex) {
            System.err.println("Ошибка при загрузке данных: " + ex.getMessage());
            JOptionPane.showMessageDialog(null, "Ошибка при загрузке данных: " + ex.getMessage() + "\nПроверьте файл test.dat и его расположение.", "Ошибка загрузки", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        if (Bank.getNumberOfCustomers() == 0) {
            JOptionPane.showMessageDialog(null, "Нет клиентов для отображения/отчета. Возможно, файл test.dat пуст или имеет неверный формат.", "Нет данных", JOptionPane.WARNING_MESSAGE);
        }

        SWINGDemo demo = new SWINGDemo();
        demo.launchFrame();
    }
}