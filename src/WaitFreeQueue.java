
class WaitFreeQueue<T> {
  private volatile int head = 0;
  private volatile T[] items;
  private volatile int tail = 0;
  @SuppressWarnings({"unchecked"})
  public WaitFreeQueue(int capacity) {
    items = (T[]) new Object[capacity];
  }
  public void enq(T x) throws FullException {
    if (((tail - head)) >= items.length)
      throw new FullException();
    else {
      items[(tail) % items.length] = x;
      tail++;
    }
  }
  public T deq() throws EmptyQueueException {
    if (tail - head <= 0)
      throw new EmptyQueueException();
    else {
      T x = items[(head) % items.length];
      head++;
      return x;
    }
  }
}


class FullException extends Exception {
  private static final long serialVersionUID = 1L;
  public FullException() {
    super();
  } 
}

class EmptyQueueException extends Exception {
  private static final long serialVersionUID = 1L;
  public EmptyQueueException() {
    super();
  } 
}

