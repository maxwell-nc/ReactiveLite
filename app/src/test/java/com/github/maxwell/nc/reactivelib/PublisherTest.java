package com.github.maxwell.nc.reactivelib;

import com.github.maxwell.nc.reactivelib.callback.Action;
import com.github.maxwell.nc.reactivelib.callback.Consumer;
import com.github.maxwell.nc.reactivelib.callback.Function;
import com.github.maxwell.nc.reactivelib.callback.Predicate;
import com.github.maxwell.nc.reactivelib.scheduler.Schedulers;
import com.github.maxwell.nc.reactivelib.subscription.Subscription;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;


/**
 * 生产者测试类
 */
public class PublisherTest {

    @Test
    public void empty() throws Exception {
        Publisher.empty().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) {
                fail();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                fail();
            }
        });
    }

    @Test
    public void error() throws Exception {
        final NullPointerException testException = new NullPointerException("test");
        Publisher.error(testException)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        fail();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        assertEquals(testException, throwable);
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        fail();
                    }
                });
    }

    @Test
    public void just() throws Exception {
        final boolean[] isComplete = {false};
        Publisher.just(1, 2, 3)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        assertFalse(isComplete[0]);//已经调用了onComplete
                        assertFalse(
                                !(integer == 1 || integer == 2 || integer == 3)
                        );
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        fail();
                    }
                }, new Action() {
                    @Override
                    public void run() {
                        isComplete[0] = true;
                    }
                });
    }

    @Test
    public void from() throws Exception {
        final List<Integer> list = Arrays.asList(1, 3, 2, 4);
        Publisher.from(list)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        assertFalse(!list.contains(integer));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        fail();
                    }
                });
    }

    @Test
    public void timer() throws Exception {
        final int[] count = {0};
        final int request = 10;
        final int cancelTime = 5;
        Publisher.timer(100)
                .subscribe(new FlowSubscriber<Long>() {
                    @Override
                    protected long getRequestCount() {
                        return request;
                    }

                    @Override
                    public void onNext(Long item) {
                        //0,1,2,3,4...
                        assertFalse(item >= request);
                        count[0]++;
                        //test cancel task
                        if (count[0] == cancelTime) {
                            cancelTask();
                        }
                    }

                    @Override
                    public void onComplete() {
                        assertEquals(count[0], cancelTime);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail();
                    }

                });
    }

    @Test
    public void map() throws Exception {
        Publisher.just(1)
                .map(new Function<Integer, String>() {
                    @Override
                    public String apply(Integer integer) throws Exception {
                        return "a";
                    }
                })
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        assertEquals(s, "a");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        fail();
                    }
                });
    }

    @Test
    public void buffer() throws Exception {
        final List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
        Publisher.from(list)
                .buffer(3)
                .subscribe(new Consumer<List<Integer>>() {
                    @Override
                    public void accept(List<Integer> integers) {
                        assertNotNull(integers);
                        assertFalse(integers.isEmpty());
                        assertFalse(integers.size() > 3);
                        assertFalse(!list.containsAll(integers));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        fail();
                    }
                });
    }

    @Test
    public void select() throws Exception {
        Publisher.just(1, 2, 3)
                .select(new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer >= 2;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        assertFalse(integer < 2);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        fail();
                    }
                });
    }

    @Test
    public void retry() throws Exception {
        final int[] sum = {0};//计算次数
        final RuntimeException testException = new RuntimeException("test");
        Publisher.just(1, 2, 3)
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        if (integer == 2) {
                            throw testException;
                        }
                        return integer;
                    }
                })
                .retry(2)
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        //1....1....1...onError
                        assertFalse(integer >= 2);
                        sum[0] += integer;
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        assertEquals(testException, throwable);
                        assertEquals(sum[0], 3);
                    }

                }, new Action() {
                    @Override
                    public void run() {
                        fail();
                    }
                });
    }

    @Test
    public void errorReturn() throws Exception {
        final RuntimeException testException = new RuntimeException("test");
        Publisher.just(1, 2, 3)
                .map(new Function<Integer, Integer>() {
                    @Override
                    public Integer apply(Integer integer) throws Exception {
                        if (integer == 2) {
                            throw testException;
                        }
                        return integer;
                    }
                })
                .errorReturn(new Function<Throwable, Integer>() {
                    @Override
                    public Integer apply(Throwable throwable) throws Exception {
                        assertEquals(testException, throwable);
                        return -1;
                    }
                })
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) {
                        assertFalse(!(integer == 1 || integer == -1));
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        fail();
                    }
                });
    }

    @Test
    public void subscribeOn() throws Exception {
        final CountDownLatch latch = new CountDownLatch(4);//异步阻塞用
        final Thread[] poolThread = new Thread[1];//新线程
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                poolThread[0] = new Thread(r);
                return poolThread[0];
            }
        });
        Publisher.just(1, 2, 3)
                .subscribeOn(Schedulers.form(executor))
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(3);
                        assertEquals(poolThread[0], Thread.currentThread());
                    }

                    @Override
                    public void onNext(Integer integer) {
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail();
                    }
                });
        latch.await();
    }

    @Test
    public void observeOn() throws Exception {
        final CountDownLatch latch = new CountDownLatch(4);//异步阻塞用
        final Thread origin = Thread.currentThread();//原始线程
        final Thread[] poolThread = new Thread[1];//新线程
        ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                poolThread[0] = new Thread(r);
                return poolThread[0];
            }
        });
        Publisher.just(1, 2, 3)
                .observeOn(Schedulers.form(executor))
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(3);
                        assertEquals(origin, Thread.currentThread());
                    }

                    @Override
                    public void onNext(Integer integer) {
                        assertEquals(poolThread[0], Thread.currentThread());
                        latch.countDown();
                    }

                    @Override
                    public void onComplete() {
                        assertEquals(poolThread[0], Thread.currentThread());
                        latch.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        fail();
                    }
                });
        latch.await();
    }

}