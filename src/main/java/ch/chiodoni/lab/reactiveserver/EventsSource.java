package ch.chiodoni.lab.reactiveserver;

import org.reactivestreams.Publisher;

public interface EventsSource {

    public Publisher<Event> events(int mod, int nr);

}
