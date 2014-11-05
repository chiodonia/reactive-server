package ch.chiodoni.lab.reactiveserver;

public class Event extends Object {

    private int nr;

    public Event(int nr) {
        this.nr = nr;
    }

    public int getNr() {
        return nr;
    }

}
