package edu.upenn.cis.cis455.m1.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ThreadPool {
    final static Logger logger = LogManager.getLogger(ThreadPool.class);
    private int size;
    private HttpTaskQueue hq;
    private List<Thread> threads;
    private List<HttpWorker> workers;

    public HttpTaskQueue getHq() {
        return hq;
    }

    public List<Thread> getThreads() {
        return threads;
    }

    public List<HttpWorker> getWorkers() {
        return this.workers;
    }

    public ThreadPool(int size, HttpTaskQueue taskQueue) {
        this.size = size;
        this.hq = taskQueue;
        threads = new ArrayList<>();
        workers = new ArrayList<>();

    }
    public void start(){
        for(int i = 0; i < size; i++){
            HttpWorker w = new HttpWorker(hq);
            Thread t = new Thread(w);
            workers.add(w);
            threads.add(t);
            w.setId(t.getName());
            logger.debug(t.getName());
            t.start();
        }
    }


    public void shutdown(){
        for(HttpWorker t : workers){
            t.shutdown();
        }
    }

}
