package com.moon.cloud.thread.mail;

public class MailSender implements Runnable {

    @Override
    public void run() {
        while (true) {
            System.out.println("send mail");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
