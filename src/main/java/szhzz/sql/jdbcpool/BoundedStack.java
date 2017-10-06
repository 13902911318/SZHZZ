package szhzz.sql.jdbcpool;

import EDU.oswego.cs.dl.util.concurrent.BoundedChannel;
import EDU.oswego.cs.dl.util.concurrent.DefaultChannelCapacity;
import EDU.oswego.cs.dl.util.concurrent.LinkedNode;

import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: SZHZZ
 * Date: 12-11-5
 * Time: 下午1:28
 * To change this template use File | Settings | File Templates.
 */

public class BoundedStack implements BoundedChannel {


    /**
     * Helper monitor. Ensures that only one in at a time executes.
     */

    protected final Object putGuard_ = new Object();
    /**
     * Helper monitor. Protects and provides wait queue for takes
     */

    protected final Object takeGuard_ = new Object();
    /**
     * Dummy header node of list. The first actual node, if it exists, is always
     * at head_.next. After each take, the old first node becomes the head.
     */
    protected LinkedNode head_;
    /**
     * The last node of list. Put() appends to list, so modifies last_
     */
    protected LinkedNode last_;
    /**
     * Number of elements allowed *
     */
    protected int capacity_;


    /**
     * One side of a split permit count.
     * The counts represent permits to do a in. (The queue is full when zero).
     * Invariant: putSidePutPermits_ + takeSidePutPermits_ = capacity_ - length.
     * (The length is never separately recorded, so this cannot be
     * checked explicitly.)
     * To minimize contention between puts and takes, the
     * in side uses up all of its permits before transfering them from
     * the take side. The take side just increments the count upon each take.
     * Thus, most puts and take can run independently of each other unless
     * the queue is empty or full.
     * Initial value is queue capacity.
     */

    protected int putSidePutPermits_;

    /**
     * Number of takes since last reconcile *
     */
    protected int takeSidePutPermits_ = 0;


    /**
     * Create a queue with the given capacity
     *
     * @throws IllegalArgumentException if capacity less or equal to zero
     */
    public BoundedStack(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException();
        capacity_ = capacity;
        putSidePutPermits_ = capacity;
        head_ = new LinkedNode(null);
        last_ = head_;
    }

    /**
     * Create a queue with the current default capacity
     */

    public BoundedStack() {
        this(DefaultChannelCapacity.get());
    }

    /**
     * Move in permits from take side to in side;
     * return the number of in side permits that are available.
     * Call only under synch on puGuard_ AND this.
     */
    protected final int reconcilePutPermits() {
        putSidePutPermits_ += takeSidePutPermits_;
        takeSidePutPermits_ = 0;
        return putSidePutPermits_;
    }


    /**
     * Return the current capacity of this queue *
     */
    public synchronized int capacity() {
        return capacity_;
    }


    /**
     * Return the number of elements in the queue.
     * This is only a snapshot value, that may be in the midst
     * of changing. The returned value will be unreliable in the presence of
     * active puts and takes, and should only be used as a heuristic
     * estimate, for example for resource monitoring purposes.
     */
    public synchronized int size() {
        /*
          This should ideally synch on putGuard_, but
          doing so would cause it to block waiting for an in-progress
          in, which might be stuck. So we instead use whatever
          value of putSidePutPermits_ that we happen to read.
        */
        return capacity_ - (takeSidePutPermits_ + putSidePutPermits_);
    }


    /**
     * Reset the capacity of this queue.
     * If the new capacity is less than the old capacity,
     * existing elements are NOT removed, but
     * incoming puts will not proceed until the number of elements
     * is less than the new capacity.
     *
     * @throws IllegalArgumentException if capacity less or equal to zero
     */

    public void setCapacity(int newCapacity) {
        if (newCapacity <= 0) throw new IllegalArgumentException();
        synchronized (putGuard_) {
            synchronized (this) {
                takeSidePutPermits_ += (newCapacity - capacity_);
                capacity_ = newCapacity;

                // Force immediate reconcilation.
                reconcilePutPermits();
                notifyAll();
            }
        }
    }


    /**
     * Main mechanics for take/poll *
     */
    protected synchronized Object extract() {
        synchronized (head_) {
            Object x = null;
            LinkedNode first = head_.next;
            if (first != null) {
                x = first.value;
                first.value = null;
                head_ = first;
                ++takeSidePutPermits_;
                notify();
            }
            return x;
        }
    }


    public Object peek() {
        synchronized (head_) {
//            LinkedNode first = head_.next;
            LinkedNode first = head_;
            if (first != null)
                return first.value;
            else
                return null;
        }
    }

    public Object take() throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        Object x = extract();
        if (x != null)
            return x;
        else {
            synchronized (takeGuard_) {
                try {
                    for (; ; ) {
                        x = extract();
                        if (x != null) {
                            return x;
                        } else {
                            takeGuard_.wait();
                        }
                    }
                } catch (InterruptedException ex) {
                    takeGuard_.notify();
                    throw ex;
                }
            }
        }
    }

    public Object poll(long msecs) throws InterruptedException {
        if (Thread.interrupted()) throw new InterruptedException();
        Object x = extract();
        if (x != null)
            return x;
        else {
            synchronized (takeGuard_) {
                try {
                    long waitTime = msecs;
                    long start = (msecs <= 0) ? 0 : System.currentTimeMillis();
                    for (; ; ) {
                        x = extract();
                        if (x != null || waitTime <= 0) {
                            return x;
                        } else {
                            takeGuard_.wait(waitTime);
                            waitTime = msecs - (System.currentTimeMillis() - start);
                        }
                    }
                } catch (InterruptedException ex) {
                    takeGuard_.notify();
                    throw ex;
                }
            }
        }
    }

    /**
     * Notify a waiting take if needed *
     */
    protected final void allowTake() {
        synchronized (takeGuard_) {
            takeGuard_.notify();
        }
    }


    /**
     * Create and insert a node.
     * Call only under synch on putGuard_
     */
    protected void insert_(Object x) {
        --putSidePutPermits_;
        LinkedNode p = new LinkedNode(x);
        synchronized (last_) {
            p.next = last_;
            last_ = p;
        }
    }

    protected void insert(Object x) {
        --putSidePutPermits_;
        LinkedNode p = new LinkedNode(x);
        synchronized (head_) {
            p.next = head_.next;
            head_.next = p;
        }
    }

    /*
       in and offer(ms) differ only in policy before insert/allowTake
    */

    public void put(Object x) throws InterruptedException {
        if (x == null) throw new IllegalArgumentException();
        if (Thread.interrupted()) throw new InterruptedException();

        synchronized (putGuard_) {

            if (putSidePutPermits_ <= 0) { // wait for permit.
                synchronized (this) {
                    if (reconcilePutPermits() <= 0) {
                        try {
                            for (; ; ) {
                                wait();
                                if (reconcilePutPermits() > 0) {
                                    break;
                                }
                            }
                        } catch (InterruptedException ex) {
                            notify();
                            throw ex;
                        }
                    }
                }
            }
            insert(x);
        }
        // call outside of lock to loosen in/take coupling
        allowTake();
    }

    public boolean offer(Object x, long msecs) throws InterruptedException {
        if (x == null) throw new IllegalArgumentException();
        if (Thread.interrupted()) throw new InterruptedException();

        synchronized (putGuard_) {

            if (putSidePutPermits_ <= 0) {
                synchronized (this) {
                    if (reconcilePutPermits() <= 0) {
                        if (msecs <= 0)
                            return false;
                        else {
                            try {
                                long waitTime = msecs;
                                long start = System.currentTimeMillis();

                                for (; ; ) {
                                    wait(waitTime);
                                    if (reconcilePutPermits() > 0) {
                                        break;
                                    } else {
                                        waitTime = msecs - (System.currentTimeMillis() - start);
                                        if (waitTime <= 0) {
                                            return false;
                                        }
                                    }
                                }
                            } catch (InterruptedException ex) {
                                notify();
                                throw ex;
                            }
                        }
                    }
                }
            }

            insert(x);
        }

        allowTake();
        return true;
    }

    public boolean isEmpty() {
        synchronized (head_) {
            return head_.next == null;
        }
    }

    public LinkedList peekAll() {
        LinkedList v = new LinkedList();
        LinkedNode point = head_;
        if (point.value != null) v.add(point.value);
        while (point.next != null) {
            point = point.next;
            if (point.value != null) v.add(point.value);
        }
        return v;
    }
}

