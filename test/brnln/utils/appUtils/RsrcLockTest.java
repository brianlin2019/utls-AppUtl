/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package brnln.utils.appUtils;

import java.util.concurrent.locks.ReentrantLock;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author brian
 */
public class RsrcLockTest {

    public RsrcLockTest() {
    }

    @Test
    public void test_getLock_001() {
        class MyData {

            int value = 0;
            boolean thd1GetLock = false;
            boolean thd2GetLock = false;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        final RsrcLock rcrcLock = RsrcLock.buildRcrcLock(reentrantLock, 3);
        final DoneCondition condThd01_hold = DoneCondition.buildCond(reentrantLock, true);
        final DoneCondition condThd01_return = DoneCondition.buildCond(reentrantLock, true);
        final DoneCondition condThd02_hold = DoneCondition.buildCond(reentrantLock, true);
        final DoneCondition condThd02_return = DoneCondition.buildCond(reentrantLock, true);
        final MyData myData = new MyData();
        //
        Thread thd1 = new Thread(new Runnable() {
            @Override
            public void run() {
                //  ----  取得 RcrcLock (start) ----
                boolean getLock = rcrcLock.holdLock(new Runnable() {
                    @Override
                    public void run() {
                        myData.value++;
                        condThd01_hold.signalAll();
                    }
                }, 2, 1000L);
                myData.thd1GetLock = getLock;
                //  ----  取得 RcrcLock ( end ) ----
                //
                if (getLock) {
                    //  模擬其他業務操作
                    AppUtils.mySleep(500L);
                    //
                    //  ----  返還 RcrcLock (start) ----
                    rcrcLock.returnLock(new Runnable() {
                        @Override
                        public void run() {
                            myData.value++;
                            condThd01_return.signalAll();
                        }
                    });
                    //  ----  返還 RcrcLock ( end ) ----
                    //
                }
            }
        });
        Thread thd2 = new Thread(new Runnable() {
            @Override
            public void run() {
                //  讓 Thread 2 搶不到 RcrcLock
                AppUtils.mySleep(30L);
                //
                //  ----  取得 RcrcLock (start) ----
                boolean getLock = rcrcLock.holdLock(new Runnable() {
                    @Override
                    public void run() {
                        myData.value++;
                        condThd02_hold.signalAll();
                    }
                }, 2, 1000L);
                myData.thd2GetLock = getLock;
                //  ----  取得 RcrcLock ( end ) ----
                //
                if (getLock) {
                    //  模擬其他業務操作
                    AppUtils.mySleep(500L);
                    //
                    //  ----  返還 RcrcLock (start) ----
                    rcrcLock.returnLock(new Runnable() {
                        @Override
                        public void run() {
                            myData.value++;
                            condThd02_return.signalAll();
                        }
                    });
                    //  ----  返還 RcrcLock ( end ) ----
                }
                //
            }
        });
        thd1.start();
        thd2.start();
        //
        Assert.assertEquals(0, myData.value);
        //
        Assert.assertTrue(!myData.thd1GetLock);
        Assert.assertTrue(condThd01_hold.waitCond(1000L * 5L));
        Assert.assertTrue(myData.thd1GetLock);
        Assert.assertEquals(1, myData.value);
        //
        Assert.assertTrue(condThd01_return.waitCond(1000L * 5L));
        Assert.assertEquals(2, myData.value);
        //
        Assert.assertTrue(!myData.thd2GetLock);
        Assert.assertTrue(condThd02_hold.waitCond(1000L * 5L));
        Assert.assertTrue(myData.thd2GetLock);
        Assert.assertEquals(3, myData.value);
        //
        Assert.assertTrue(condThd02_return.waitCond(1000L * 5L));
        Assert.assertEquals(4, myData.value);
    }

    @Test
    public void test_getLock_fail() {
        class MyData {

            boolean thd1GetLock = false;
            boolean thd2GetLock = false;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        final RsrcLock rcrcLock = RsrcLock.buildRcrcLock(reentrantLock, 3);
        final DoneCondition condThd01_hold = DoneCondition.buildCond(reentrantLock, true);
        final MyData myData = new MyData();
        //
        Thread thd1 = new Thread(new Runnable() {
            @Override
            public void run() {
                myData.thd1GetLock = rcrcLock.holdLock(null, 1, 1000L);
                AppUtils.mySleep(1000L);
                condThd01_hold.signalAll();
                rcrcLock.returnLock(null);
            }
        });
        Thread thd2 = new Thread(new Runnable() {
            @Override
            public void run() {
                AppUtils.mySleep(10L);
                myData.thd2GetLock = rcrcLock.holdLock(null, 1, 10L);
            }
        });
        //
        myData.thd2GetLock = true;
        //
        thd1.start();
        thd2.start();
        //
        Assert.assertTrue(!myData.thd1GetLock);
        Assert.assertTrue(condThd01_hold.waitCond(1000L * 5L));
        Assert.assertTrue(myData.thd1GetLock);
        //
        Assert.assertTrue(!myData.thd2GetLock);
    }

    @Test
    public void test_deadLock_abort_001() {
        class MyData {

            boolean thd1GetLock01 = false;
            boolean thd1GetLock02 = false;
            boolean thd2GetLock01 = false;
            boolean thd2GetLock02 = false;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        final RsrcLock rcrcLock01 = RsrcLock.buildRcrcLock(reentrantLock, 1);
        final RsrcLock rcrcLock02 = RsrcLock.buildRcrcLock(reentrantLock, 2);
        final DoneCondition thd1Done = DoneCondition.buildCond(reentrantLock, true);
        final DoneCondition thd2Done = DoneCondition.buildCond(reentrantLock, true);
        final MyData myData = new MyData();
        //
        Thread thd1 = new Thread(new Runnable() {
            @Override
            public void run() {
                myData.thd1GetLock01 = rcrcLock01.holdLock(null, 1, 1000L);
                if (myData.thd1GetLock01) {
                    AppUtils.mySleep(200L);
                    myData.thd1GetLock02 = rcrcLock02.holdLock(null, 1, 1000L);
                    if (myData.thd1GetLock02) {
                        AppUtils.mySleep(200L);
                        rcrcLock02.returnLock(null);
                    }
                    rcrcLock01.returnLock(null);
                }
                thd1Done.signalAll();
            }
        });
        Thread thd2 = new Thread(new Runnable() {
            @Override
            public void run() {
                myData.thd2GetLock02 = rcrcLock02.holdLock(null, 2, 1000L);
                if (myData.thd2GetLock02) {
                    AppUtils.mySleep(200L);
                    myData.thd2GetLock01 = rcrcLock01.holdLock(null, 2, 1000L);
                    if (myData.thd2GetLock01) {
                        AppUtils.mySleep(200L);
                        rcrcLock01.returnLock(null);
                    }
                    rcrcLock02.returnLock(null);
                }
                thd2Done.signalAll();
            }
        });
        //
        thd1.start();
        thd2.start();
        //
        thd1Done.waitCond(1000L * 3L);
        thd2Done.waitCond(1000L * 3L);
        //
        Assert.assertTrue(myData.thd1GetLock01);
        Assert.assertTrue(myData.thd1GetLock02);
        Assert.assertTrue(myData.thd2GetLock02);
        Assert.assertTrue(!myData.thd2GetLock01);

    }

    @Test
    public void test_deadLock_abort_002() {
        class MyData {

            boolean thd1GetLock01 = false;
            boolean thd1GetLock02 = false;
            boolean thd2GetLock02 = false;
            boolean thd2GetLock03 = false;
            boolean thd3GetLock03 = false;
            boolean thd3GetLock01 = false;
        }
        ReentrantLock reentrantLock = new ReentrantLock();
        final RsrcLock rcrcLock01 = RsrcLock.buildRcrcLock(reentrantLock, 1);
        final RsrcLock rcrcLock02 = RsrcLock.buildRcrcLock(reentrantLock, 2);
        final RsrcLock rcrcLock03 = RsrcLock.buildRcrcLock(reentrantLock, 3);
        final DoneCondition thd1Done = DoneCondition.buildCond(reentrantLock, true);
        final DoneCondition thd2Done = DoneCondition.buildCond(reentrantLock, true);
        final DoneCondition thd3Done = DoneCondition.buildCond(reentrantLock, true);
        final MyData myData = new MyData();
        //
        Thread thd1 = new Thread(new Runnable() {
            @Override
            public void run() {
                myData.thd1GetLock01 = rcrcLock01.holdLock(null, 1, 1000L);
                if (myData.thd1GetLock01) {
                    AppUtils.mySleep(200L);
                    myData.thd1GetLock02 = rcrcLock02.holdLock(null, 1, 1000L);
                    if (myData.thd1GetLock02) {
                        AppUtils.mySleep(200L);
                        rcrcLock02.returnLock(null);
                    }
                    rcrcLock01.returnLock(null);
                }
                thd1Done.signalAll();
            }
        });
        Thread thd2 = new Thread(new Runnable() {
            @Override
            public void run() {
                myData.thd2GetLock02 = rcrcLock02.holdLock(null, 2, 1000L);
                if (myData.thd2GetLock02) {
                    AppUtils.mySleep(200L);
                    myData.thd2GetLock03 = rcrcLock03.holdLock(null, 2, 1000L);
                    if (myData.thd2GetLock03) {
                        AppUtils.mySleep(200L);
                        rcrcLock03.returnLock(null);
                    }
                    rcrcLock02.returnLock(null);
                }
                thd2Done.signalAll();
            }
        });
        Thread thd3 = new Thread(new Runnable() {
            @Override
            public void run() {
                myData.thd3GetLock03 = rcrcLock03.holdLock(null, 3, 1000L);
                if (myData.thd3GetLock03) {
                    AppUtils.mySleep(200L);
                    myData.thd3GetLock01 = rcrcLock01.holdLock(null, 3, 1000L);
                    if (myData.thd3GetLock01) {
                        AppUtils.mySleep(200L);
                        rcrcLock01.returnLock(null);
                    }
                    rcrcLock03.returnLock(null);
                }
                thd3Done.signalAll();
            }
        });
        //
        thd1.start();
        thd2.start();
        thd3.start();
        //
        thd1Done.waitCond(1000L * 3L);
        thd2Done.waitCond(1000L * 3L);
        thd3Done.waitCond(1000L * 3L);
        //
        Assert.assertTrue(myData.thd1GetLock01);
        Assert.assertTrue(myData.thd1GetLock02);
        Assert.assertTrue(myData.thd2GetLock02);
        Assert.assertTrue(myData.thd2GetLock03);
        Assert.assertTrue(myData.thd3GetLock03);
        Assert.assertTrue(!myData.thd3GetLock01);

    }
}
