package com.github.maxwell.nc.reactivelib.observable.create;

import com.github.maxwell.nc.reactivelib.Publisher;
import com.github.maxwell.nc.reactivelib.Subscriber;
import com.github.maxwell.nc.reactivelib.callback.FlowStream;
import com.github.maxwell.nc.reactivelib.scheduler.Scheduler;
import com.github.maxwell.nc.reactivelib.subscription.FlowSubscription;


/**
 * 定时器数据生产者<br>
 * 按照给定{@link #interval}毫秒数间隔顺序发送从0开始递增的序列<br>
 * 例如：0、1、2、3、4、5...<br>
 * 如果不限制请求数据，则会不断产生新的数据<br>
 * 需要注意，由于产生数据操作是阻塞操作，需要设置调度器{@link #scheduler}防止主线程阻塞<br>
 * 默认情况下没有设置调度器，将会在所执行的线程生产数据<br>
 * 此生产者可以通过以下方法创建：<br>
 * {@link Publisher#timer(long)}<br>
 * {@link Publisher#timer(long, Scheduler)}<br>
 */
public class TimerPublisher extends Publisher<Long> {

    /**
     * 定时器产生数据的间隔（毫秒数）
     */
    private final long interval;
    private final Scheduler scheduler;

    public TimerPublisher(long interval, Scheduler scheduler) {
        this.interval = interval;
        this.scheduler = scheduler;
    }

    @Override
    protected void subscribeActual(Subscriber<Long> subscriber) {
        subscriber.onSubscribe(new TimerSubscription(subscriber, interval, scheduler));
    }

    private static final class TimerSubscription extends FlowSubscription implements FlowStream<Long> {

        private final Subscriber<Long> actual;
        private final long interval;
        private final Scheduler scheduler;

        private long index = 0;

        private TimerSubscription(Subscriber<Long> subscriber, long interval, Scheduler scheduler) {
            this.actual = subscriber;
            this.interval = interval;
            this.scheduler = scheduler;
        }

        @Override
        public Long next() {
            return index++;
        }

        @Override
        public void request(final long count) {
            if (scheduler != null) {
                scheduler.schedule(new Runnable() {
                    @Override
                    public void run() {
                        requestActual(count);
                    }
                });
            } else {
                requestActual(count);
            }
        }

        private void requestActual(long count) {
            try {
                while (!cancelled && index != count) {
                    actual.onNext(next());
                    Thread.sleep(interval);//当前线程进行阻塞
                }
            } catch (Exception e) {
                cancel();
                actual.onError(e);
                return;
            }

            if (cancelled) {
                actual.onComplete();
            }
        }

    }
}
