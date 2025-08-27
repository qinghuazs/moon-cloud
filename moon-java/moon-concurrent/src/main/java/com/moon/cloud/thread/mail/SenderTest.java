package com.moon.cloud.thread.mail;

public class SenderTest {
    public static void main(String[] args) throws InterruptedException {
        MailSender mailSender = new MailSender();
        Thread thread = new Thread(mailSender);
        thread.start();
        Thread.currentThread().sleep(1000 * 10L);

        thread.interrupt();
    }
}
