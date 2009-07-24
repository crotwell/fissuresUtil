package edu.sc.seis.fissuresUtil.flow;

public class Message {

    public Message(Object subject) {
        this.subject = subject;
    }

    public Object getSubject() {
        return subject;
    }

    private Object subject;
}
