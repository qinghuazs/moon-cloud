package com.moon.leetcode.lru;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache extends LinkedHashMap<Integer, Integer> {

    public static void main(String[] args) {
        LRUCache lruCache = new LRUCache();
        System.out.println(lruCache.capacity);

    }

    public LRUCache() {

    }

    private int capacity;

    public LRUCache(int capacity) {
       super(capacity, 0.75f, true);
       this.capacity = capacity;
    }

    public void put(int key, int value) {
        super.put(key, value);
    }

    public int get(int key) {
        return super.getOrDefault(key, -1);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        return size() > capacity;
    }
}
