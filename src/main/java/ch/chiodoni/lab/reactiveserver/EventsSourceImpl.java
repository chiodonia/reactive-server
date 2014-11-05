package ch.chiodoni.lab.reactiveserver;

import org.reactivestreams.Publisher;
import reactor.rx.Streams;
import reactor.rx.stream.HotStream;
import reactor.timer.SimpleHashWheelTimer;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EventsSourceImpl implements EventsSource {

    private final static long PERIORD = 100L;
    private final static int RESOLUTION = 50;
    private SimpleHashWheelTimer timer = new SimpleHashWheelTimer(RESOLUTION);
    private Random random = new Random(System.currentTimeMillis());

    public Publisher<Event> events(final int mod, final int nr) {
        final HotStream<Integer> stream = Streams.<Integer>defer();
        timer.schedule(aLong -> stream.accept(random.nextInt()), PERIORD, TimeUnit.MILLISECONDS);
        return stream
                .filter(i -> i % mod == 0)
                .map(Math::abs)
                .map(Event::new)
                .take(nr);
    }

}
