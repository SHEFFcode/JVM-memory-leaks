
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.lang.ref.WeakReference;
import java.io.*;

public class MemoryLeakExamples {

    public static void main(String[] args) throws IOException {
        System.out.println("Memory Leak Examples in Java");
        System.out.println("=============================");

        // Run examples (commented out to prevent actual leaks)
        // staticCollectionLeak();
        // listenerLeak();
        // threadLeak();
        // anonymousInnerClassLeak();
        // cacheWithoutEvictionLeak();
        // unclosedResourcesLeak();
        // hashCodeEqualsLeak();

        System.out.println("Examples created - uncomment method calls to see leaks in action");

        // Keep program running to examine results
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String line = reader.readLine();
            if (line.equals("exit")) {
                break;
            }
        }
    }

    // 1. Static Collection Leak
    static class StaticMemoryLeaker {
        private static final List<ExpensiveObject> staticList = new ArrayList<>();

        public static void addData(ExpensiveObject data) {
            staticList.add(data); // Objects never get removed!
        }

        public static int getSize() {
            return staticList.size();
        }

        // Fix method
        public static void clear() {
            staticList.clear();
        }
    }

    static class ExpensiveObject {
        private final int id;
        private final byte[] data;

        public ExpensiveObject(int id) {
            this.id = id;
            this.data = new byte[1024 * 1024]; // 1MB object
        }

        public int getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ExpensiveObject that = (ExpensiveObject) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("ExpensiveObject " + id + " is being finalized");
            super.finalize();
        }
    }

    public static void staticCollectionLeak() {
        System.out.println("\n1. Static Collection Leak Example");
        System.out.println("Adding objects to static collection...");

        for (int i = 0; i < 10; i++) {
            StaticMemoryLeaker.addData(new ExpensiveObject(i));
            System.out.println("Added object " + i + ", collection size: " +
                    StaticMemoryLeaker.getSize());
        }

        System.out.println("Objects remain in memory even after method ends!");
        // Fix: Call StaticMemoryLeaker.clear() when done
    }

    // 2. Listener/Observer Pattern Leak
    interface EventListener {
        void onEvent(String event);
    }

    static class EventPublisher {
        private final List<EventListener> listeners = new ArrayList<>();

        public void addListener(EventListener listener) {
            listeners.add(listener);
        }

        public void removeListener(EventListener listener) {
            listeners.remove(listener);
        }

        public void publishEvent(String event) {
            for (EventListener listener : listeners) {
                listener.onEvent(event);
            }
        }

        public int getListenerCount() {
            return listeners.size();
        }
    }

    static class LeakyComponent implements EventListener {
        private final byte[] heavyResource = new byte[1024 * 1024]; // 1MB
        private final int id;

        public LeakyComponent(int id) {
            this.id = id;
        }

        @Override
        public void onEvent(String event) {
            System.out.println("LeakyComponent " + id + " received: " + event);
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("LeakyComponent " + id + " is being finalized");
            super.finalize();
        }
    }

    public static void listenerLeak() {
        System.out.println("\n2. Listener/Observer Pattern Leak Example");
        EventPublisher publisher = new EventPublisher();

        // Create components that register as listeners
        List<LeakyComponent> components = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            LeakyComponent component = new LeakyComponent(i);
            publisher.addListener(component);
            components.add(component);
            System.out.println("Added listener " + i);
        }

        // Simulate clearing components but forgetting to unregister listeners
        components.clear(); // This doesn't remove them from publisher!
        System.out.println("Components cleared, but " + publisher.getListenerCount() +
                " listeners still registered in publisher!");

        // Force garbage collection to see that objects aren't collected
        System.gc();

        // Publisher still holds references to the components!
        publisher.publishEvent("Test event");

        // Fix: Always call publisher.removeListener(component) before clearing
    }

    // 3. Thread and Timer Leak
    static class ThreadLeakExample {
        private volatile boolean isRunning = true;
        private Thread backgroundThread;

        public void startBackgroundWork() {
            backgroundThread = new Thread(() -> {
                while (isRunning) {
                    try {
                        Thread.sleep(1000);
                        System.out.println("Background work running in " +
                                Thread.currentThread().getName());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
            backgroundThread.setDaemon(false); // Non-daemon thread prevents JVM shutdown
            backgroundThread.start();
        }

        public void stop() {
            isRunning = false;
            if (backgroundThread != null) {
                backgroundThread.interrupt();
            }
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("ThreadLeakExample is being finalized");
            super.finalize();
        }
    }

    public static void threadLeak() {
        System.out.println("\n3. Thread Leak Example");
        List<ThreadLeakExample> workers = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            ThreadLeakExample worker = new ThreadLeakExample();
            worker.startBackgroundWork();
            workers.add(worker);
            System.out.println("Started worker thread " + i);
        }

        // Simulate forgetting to stop threads
        workers.clear();
        System.out.println("Workers cleared, but threads still running!");

        try {
            Thread.sleep(3000); // Let threads run for a bit
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Fix: Always call worker.stop() before clearing
    }

    // 4. Anonymous Inner Class Leak (holds reference to outer class)
    static class OuterClass {
        private byte[] heavyData = new byte[1024 * 1024]; // 1MB
        private final int id;

        public OuterClass(int id) {
            this.id = id;
        }

        public Runnable createAnonymousRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    System.out.println("Anonymous runnable from outer class " + id);
                    // This anonymous class holds a reference to OuterClass instance!
                }
            };
        }

        public Runnable createLambdaRunnable() {
            return () -> {
                System.out.println("Lambda runnable from outer class " + id);
                // Lambda also captures 'this' reference when using instance fields!
            };
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("OuterClass " + id + " is being finalized");
            super.finalize();
        }
    }

    public static void anonymousInnerClassLeak() {
        System.out.println("\n4. Anonymous Inner Class Leak Example");
        List<Runnable> runnables = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            OuterClass outer = new OuterClass(i);
            Runnable runnable = outer.createAnonymousRunnable();
            runnables.add(runnable);
            System.out.println("Created anonymous runnable " + i);
            // outer goes out of scope, but anonymous class keeps it alive!
        }

        System.out.println("OuterClass instances should be GC'd but are kept alive!");

        // Force GC to demonstrate leak
        System.gc();

        // Execute runnables
        for (Runnable r : runnables) {
            r.run();
        }

        // Fix: Use static inner classes or store only needed data
    }

    // 5. Cache without Eviction Policy
    static class NaiveCacheExample {
        private final Map<String, ExpensiveObject> cache = new HashMap<>();

        public ExpensiveObject get(String key) {
            return cache.computeIfAbsent(key, k -> {
                System.out.println("Creating expensive object for key: " + k);
                return new ExpensiveObject(k.hashCode());
            });
        }

        public int getCacheSize() {
            return cache.size();
        }

        public void clear() {
            cache.clear();
        }
    }

    public static void cacheWithoutEvictionLeak() {
        System.out.println("\n5. Cache without Eviction Leak Example");
        NaiveCacheExample cache = new NaiveCacheExample();

        // Simulate accessing many different keys
        for (int i = 0; i < 100; i++) {
            cache.get("key_" + i);
            if (i % 20 == 0) {
                System.out.println("Cache size: " + cache.getCacheSize());
            }
        }

        System.out.println("Final cache size: " + cache.getCacheSize());
        System.out.println("All objects remain in cache forever!");

        // Fix: Implement LRU cache, use WeakHashMap, or add manual cleanup
    }

    // 6. Unclosed Resources Leak
    static class ResourceLeakExample {

        public static void fileResourceLeak() {
            System.out.println("\n6a. File Resource Leak Example");

            // BAD: Not closing file resources
            try {
                for (int i = 0; i < 5; i++) {
                    FileInputStream fis = new FileInputStream("nonexistent_file_" + i + ".txt");
                    // File handle is never closed!
                    System.out.println("Opened file " + i);
                }
            } catch (IOException e) {
                System.out.println("Expected IOException: " + e.getMessage());
            }

            System.out.println("File handles may still be open!");
        }

        public static void timerResourceLeak() {
            System.out.println("\n6b. Timer Resource Leak Example");
            List<Timer> timers = new ArrayList<>();

            for (int i = 0; i < 3; i++) {
                Timer timer = new Timer("Timer-" + i);
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Timer task executed by " + Thread.currentThread().getName());
                    }
                }, 1000, 2000);

                timers.add(timer);
            }

            // Simulate forgetting to cancel timers
            timers.clear(); // Timers still running!
            System.out.println("Timer references cleared, but timers still running!");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Fix: Always call timer.cancel()
        }
    }

    public static void unclosedResourcesLeak() {
        ResourceLeakExample.fileResourceLeak();
        ResourceLeakExample.timerResourceLeak();
    }

    // 7. HashCode/Equals Contract Violation Leak
    static class BadHashCodeObject {
        private int value;

        public BadHashCodeObject(int value) {
            this.value = value;
        }

        public void setValue(int value) {
            this.value = value; // Changing value after adding to Set/Map!
        }

        public int getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BadHashCodeObject that = (BadHashCodeObject) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        protected void finalize() throws Throwable {
            System.out.println("BadHashCodeObject with value " + value + " is being finalized");
            super.finalize();
        }
    }

    public static void hashCodeEqualsLeak() {
        System.out.println("\n7. HashCode/Equals Contract Violation Leak Example");
        Set<BadHashCodeObject> set = new HashSet<>();
        List<BadHashCodeObject> objects = new ArrayList<>();

        // Add objects to set
        for (int i = 0; i < 5; i++) {
            BadHashCodeObject obj = new BadHashCodeObject(i);
            set.add(obj);
            objects.add(obj);
            System.out.println("Added object with value " + i + " to set");
        }

        System.out.println("Set size before modification: " + set.size());

        // Modify objects after adding to set (breaks hashCode contract!)
        for (BadHashCodeObject obj : objects) {
            obj.setValue(obj.getValue() + 100);
        }

        System.out.println("Modified all object values");

        // Try to remove objects - this will fail because hashCode changed!
        objects.clear();
        for (BadHashCodeObject obj : new ArrayList<>(set)) {
            boolean removed = set.remove(obj);
            System.out.println("Attempted to remove object: " + removed);
        }

        System.out.println("Set size after removal attempts: " + set.size());
        System.out.println("Objects are stuck in the set and cannot be removed!");

        // Force GC
        System.gc();

        // Fix: Never modify objects used as keys in HashSet/HashMap
    }

    // 8. Proper Resource Management Examples
    static class ProperResourceManagement implements AutoCloseable {
        private final List<ExpensiveObject> resources = new ArrayList<>();
        private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        public void addResource(ExpensiveObject resource) {
            resources.add(resource);
        }

        public void startPeriodicTask() {
            executor.scheduleAtFixedRate(() -> {
                System.out.println("Periodic task executed, managing " + resources.size() + " resources");
            }, 0, 1, TimeUnit.SECONDS);
        }

        @Override
        public void close() {
            resources.clear();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Resources properly cleaned up");
        }
    }

    public static void properResourceManagementExample() {
        System.out.println("\n8. Proper Resource Management Example");

        try (ProperResourceManagement manager = new ProperResourceManagement()) {
            for (int i = 0; i < 5; i++) {
                manager.addResource(new ExpensiveObject(i));
            }
            manager.startPeriodicTask();
            System.out.println("Resources added to manager");

            Thread.sleep(3000); // Let it run for a bit
        } catch (Exception e) {
            e.printStackTrace();
        } // Automatically calls close() here

        System.out.println("Resources cleaned up automatically!");
    }

    // 9. Memory Leak Detection Helper
    static class MemoryLeakDetector {
        private final Map<String, Integer> objectCounts = new HashMap<>();

        public synchronized void trackObject(String className) {
            objectCounts.put(className, objectCounts.getOrDefault(className, 0) + 1);
        }

        public synchronized void untrackObject(String className) {
            Integer count = objectCounts.get(className);
            if (count != null && count > 0) {
                objectCounts.put(className, count - 1);
            }
        }

        public synchronized void printStats() {
            System.out.println("\nObject counts:");
            objectCounts.forEach((className, count) ->
                    System.out.println(className + ": " + count + " instances"));
        }
    }

    // 10. WeakReference Example (Prevention)
    static class WeakReferenceExample {
        private final Map<String, WeakReference<ExpensiveObject>> cache = new HashMap<>();

        public ExpensiveObject get(String key) {
            WeakReference<ExpensiveObject> ref = cache.get(key);
            ExpensiveObject obj = (ref != null) ? ref.get() : null;

            if (obj == null) {
                obj = new ExpensiveObject(key.hashCode());
                cache.put(key, new WeakReference<>(obj));
                System.out.println("Created new object for key: " + key);
            } else {
                System.out.println("Retrieved cached object for key: " + key);
            }

            return obj;
        }

        public void cleanup() {
            cache.entrySet().removeIf(entry -> entry.getValue().get() == null);
        }

        public int getCacheSize() {
            return cache.size();
        }
    }

    public static void weakReferenceExample() {
        System.out.println("\n10. WeakReference Cache Example (Memory Leak Prevention)");
        WeakReferenceExample cache = new WeakReferenceExample();

        // Create some objects
        for (int i = 0; i < 5; i++) {
            ExpensiveObject obj = cache.get("key_" + i);
        }

        System.out.println("Cache size: " + cache.getCacheSize());

        // Force garbage collection
        System.gc();
        Thread.yield(); // Give GC a chance to run

        // Check cache again
        cache.cleanup(); // Remove dead references
        System.out.println("Cache size after GC and cleanup: " + cache.getCacheSize());

        System.out.println("WeakReferences allow objects to be garbage collected!");
    }
}