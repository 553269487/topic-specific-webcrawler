package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.storage.StorageDB;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ThreadPool {
    final static Logger logger = LogManager.getLogger(ThreadPool.class);
    private int size;
    private BlockingQueue<String> blockingQueue;
    private Crawler master;
    private StorageDB db;
    private List<Thread> threads;
    private List<Worker> workers;

    public BlockingQueue getHq() {
        return blockingQueue;
    }

    public List<Thread> getThreads() {
        return threads;
    }


    public ThreadPool(int size, BlockingQueue blockingQueue, StorageDB db, Crawler master) {
        this.size = size;
        this.blockingQueue = blockingQueue;
        this.db = db;
        this.master = master;
        threads = new ArrayList<>();
        workers = new ArrayList<>();

    }
    public void start(){
        for(int i = 0; i < size; i++){
            Worker w = new Worker(blockingQueue, db, master);
            Thread t = new Thread(w);
            workers.add(w);
            threads.add(t);
            w.setId("Thread "+ i);
            t.start();
        }
        for(int i = 0; i < size; i++){
            try {
                threads.get(i).join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void shutdown(){
        for(Worker t : workers){
            t.shutdown();
        }
    }

}
