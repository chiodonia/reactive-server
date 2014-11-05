package ch.chiodoni.lab.reactiveserver;

import com.google.inject.AbstractModule;

public class EventsSourceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(EventsSource.class).to(EventsSourceImpl.class);
    }

}
