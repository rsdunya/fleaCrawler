package com.flea;

import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by bilgi on 8/23/15.
 */
public class HazelThreadTestMain {

    private static Config cfg = new Config();
    private static HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
    private static List<String> hzTestList = instance.getList("hzTestList");

    public static void main(String[] args){
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for(int i=0; i < 10; ++i){
            executorService.submit(new HazelThreadTest(i));
        }
    }


}

class HazelThreadTest implements Runnable{
    private Config cfg = new Config();
    private HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
    private List<String> hzTestList = instance.getList("hzTestList");

    int id;
    public HazelThreadTest(int i){
        cfg.setProperty("hazelcast.logging.type", "none");
        id = i;
    }

    @Override
    public void run() {
        hzTestList.add(String.valueOf("Id: " + id));
        System.out.print(id + " id: ");
        for(int i=0; i < hzTestList.size(); ++i){
            System.out.print(hzTestList.get(i) + ",");
        }
    }
}