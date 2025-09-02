``` markdown
# Memory Leak Examples: Java & Kotlin

A comprehensive educational repository demonstrating common memory leak patterns in both Java and Kotlin, with practical examples and prevention strategies.

## 📋 Table of Contents

- [Overview](#overview)
- [Memory Leak Fundamentals](#memory-leak-fundamentals)
- [Java vs Kotlin Memory Leaks](#java-vs-kotlin-memory-leaks)
- [Common Memory Leak Types](#common-memory-leak-types)
- [Project Structure](#project-structure)
- [How to Run](#how-to-run)
- [Monitoring Memory Usage](#monitoring-memory-usage)
- [Prevention Strategies](#prevention-strategies)
- [Additional Resources](#additional-resources)

## 🎯 Overview

Memory leaks occur when applications retain object references longer than necessary, preventing the garbage collector from reclaiming memory. This repository provides hands-on examples of the most common memory leak patterns in both Java and Kotlin, helping developers understand, identify, and prevent these issues in their applications.

## 🧠 Memory Leak Fundamentals

### What is a Memory Leak?

A memory leak happens when:
1. Objects are allocated in memory
2. Objects are no longer needed by the application
3. Objects remain referenced and cannot be garbage collected
4. Available memory gradually decreases over time

### Why Memory Leaks Matter

- **Performance degradation**: Applications slow down as available memory decreases
- **OutOfMemoryError**: Applications crash when heap space is exhausted
- **Resource waste**: Server resources are consumed unnecessarily
- **Scalability issues**: Applications cannot handle expected load

## ⚖️ Java vs Kotlin Memory Leaks

### Similarities

Both Java and Kotlin run on the JVM and share common memory leak patterns:

| Pattern | Java | Kotlin | Description |
|---------|------|--------|-------------|
| **Static Collections** | ✅ | ✅ | Objects stored in static collections persist for application lifetime |
| **Listener Leaks** | ✅ | ✅ | Unregistered listeners prevent garbage collection |
| **Thread Leaks** | ✅ | ✅ | Non-daemon threads keep objects alive |
| **Cache Leaks** | ✅ | ✅ | Unbounded caches grow indefinitely |
| **Resource Leaks** | ✅ | ✅ | Unclosed files, streams, connections |

### Key Differences

| Aspect | Java | Kotlin |
|--------|------|--------|
| **Anonymous Classes** | More prone to outer class capture | Less common due to lambda preference |
| **Closure Capture** | Limited lambda capture scope | Full lexical scope capture in lambdas |
| **Resource Management** | try-with-resources | `use` extension function |
| **Null Safety** | Manual null checks | Built-in null safety reduces some leak vectors |
| **Extension Functions** | N/A | Can create unexpected object references |

### Language-Specific Patterns

#### Java-Specific
- **Anonymous Inner Classes**: Always hold implicit reference to outer class
- **Non-static Inner Classes**: Automatic outer class reference
- **Finalize Methods**: Can prevent timely garbage collection
- **String Interning**: Strings added to intern pool persist

#### Kotlin-Specific
- **Closure Capture**: Lambdas capture entire lexical scope by default
- **Object Declarations**: Singleton objects persist for application lifetime
- **Extension Functions**: Can inadvertently extend object lifecycles
- **Coroutine Context**: Improper coroutine lifecycle management

## 🐛 Common Memory Leak Types

### 1. Static Collection Leak
Objects added to static collections are never garbage collected.

**Impact**: High - Objects persist for entire application lifecycle
**Detection**: Monitor static collection sizes over time

### 2. Listener/Observer Pattern Leak
Components register as listeners but forget to unregister.

**Impact**: Medium-High - Accumulates over time with user interactions
**Detection**: Track listener registration vs. unregistration

### 3. Thread Leak
Background threads that aren't properly stopped continue running.

**Impact**: High - Threads prevent JVM shutdown and hold object references
**Detection**: Monitor active thread count

### 4. Anonymous Inner Class / Closure Capture Leak
Inner classes or closures hold references to outer scope objects.

**Impact**: Medium - Depends on outer object size and closure lifecycle
**Detection**: Heap dumps showing unexpected object retention

### 5. Cache without Eviction Policy
Caches that grow indefinitely without cleanup policies.

**Impact**: High - Memory usage grows continuously
**Detection**: Monitor cache size metrics

### 6. Unclosed Resources Leak
File handles, database connections, streams not properly closed.

**Impact**: High - Can exhaust system resources
**Detection**: Monitor file descriptors, connection pools

### 7. HashCode/Equals Contract Violation (Java)
Objects stuck in collections due to modified hash codes.

**Impact**: Medium - Objects become unreachable but not collectable
**Detection**: Collections that never shrink despite removals

## 📁 Project Structure
```
memory-leak-examples/ ├── README.md ├── MemoryLeakExamples.java # Java examples ├── MemoryLeakExamples.kt # Kotlin examples └── monitoring/ ├── jvm-options.txt # JVM monitoring flags └── profiling-guide.md # Memory profiling instructions
``` 

## 🚀 How to Run

### Prerequisites

- Java 21 or higher
- Kotlin 2 or higher
- IDE with JVM support (IntelliJ IDEA, Eclipse, VS Code)

### Running Java Examples

1. **Compile and run:**
   ```bash
   javac MemoryLeakExamples.java
   java MemoryLeakExamples
   ```

2. **Enable specific examples:**
   Edit `MemoryLeakExamples.java` and uncomment the method calls in `main()`:
   ```java
   public static void main(String[] args) {
       // Uncomment one at a time to see individual effects
       staticCollectionLeak();
       // listenerLeak();
       // threadLeak();
       // etc.
   }
   ```

3. **Monitor with JVM flags:**
   ```bash
   java -Xms256m -Xmx1g -XX:+PrintGC -XX:+PrintGCDetails MemoryLeakExamples
   ```

### Running Kotlin Examples

1. **Compile and run:**
   ```bash
   kotlinc MemoryLeakExamples.kt -include-runtime -d MemoryLeakExamples.jar
   java -jar MemoryLeakExamples.jar
   ```

2. **Interactive mode:**
   The Kotlin version includes an interactive menu - just run and follow prompts:
   ```
   Choose an example to run:
   1. Static Collection Leak
   2. Listener/Callback Leak
   3. Thread Leak
   4. Closure Capture Leak
   5. Cache without Eviction
   6. Proper Resource Management
   0. Exit
   ```

3. **With memory monitoring:**
   ```bash
   java -Xms256m -Xmx1g -XX:+PrintGC -jar MemoryLeakExamples.jar
   ```

### IDE Setup

1. **IntelliJ IDEA:**
    - Open project folder
    - Right-click on Java/Kotlin files → Run
    - Use built-in profiler: Run → Profile

2. **Eclipse:**
    - Import as Java project
    - Right-click → Run As → Java Application
    - Use Memory Analyzer Tool (MAT) for heap analysis

## 📊 Monitoring Memory Usage

### JVM Flags for Memory Analysis

Add these flags when running examples:

```bash
# Basic GC logging
-XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps

# Heap dumps on OutOfMemoryError
-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heap-dumps/

# Memory usage tracking
-XX:+PrintStringDeduplicationStatistics
-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap
```
```
### Monitoring Tools
1. **Built-in Tools:**
    - `jps` - List Java processes
    - `jstat` - JVM statistics
    - `jmap` - Memory map and heap dumps
    - `jvisualvm` - Visual profiling tool

2. **Command Examples:**
``` bash
   # Monitor GC activity
   jstat -gc -t [PID] 5s
   
   # Create heap dump
   jmap -dump:format=b,file=heap.hprof [PID]
   
   # Monitor memory usage
   jstat -gccapacity [PID]
```
1. **Professional Tools:**
    - **JProfiler**: Commercial profiling tool
    - **YourKit**: Advanced memory analysis
    - **Eclipse MAT**: Free memory analyzer
    - **VisualVM**: Free profiling tool

### Expected Behavior
When running leak examples, you should observe:
1. **Memory Growth**: Heap usage increases over time
2. **GC Frequency**: More frequent garbage collection attempts
3. **GC Duration**: Longer garbage collection times
4. **Object Retention**: Objects that should be collected remain in memory

## 🛡️ Prevention Strategies
### Universal Strategies (Java & Kotlin)
1. **Resource Management**
``` java
   // Java - try-with-resources
   try (FileInputStream fis = new FileInputStream("file.txt")) {
       // Use resource
   } // Automatically closed
```
``` kotlin

   // Kotlin - use extension
   FileInputStream("file.txt").use { fis ->
       // Use resource
   } // Automatically closed
```
1. **Listener Management**
``` java
   // Always pair registration with unregistration
   publisher.addListener(listener);
   // Later...
   publisher.removeListener(listener);
```
1. **Thread Lifecycle**
``` java
   // Use daemon threads for background work
   thread.setDaemon(true);
   // Or properly shutdown executor services
   executorService.shutdown();
```
1. **Cache Management**
``` java
   // Use bounded caches
   Cache<String, Object> cache = CacheBuilder.newBuilder()
       .maximumSize(1000)
       .expireAfterWrite(10, TimeUnit.MINUTES)
       .build();
```
### Java-Specific Prevention
1. **Static Inner Classes**
``` java
   // Prefer static inner classes
   static class SafeInnerClass {
       // No implicit outer class reference
   }
```
1. **Weak References**
``` java
   // Use WeakHashMap for caches
   Map<String, Object> cache = new WeakHashMap<>();
   
   // Use WeakReference for optional references
   WeakReference<Object> ref = new WeakReference<>(object);
```
### Kotlin-Specific Prevention
1. **Careful Closure Capture**
``` kotlin
   // BAD: Captures entire scope
   fun createCallback(): () -> Unit {
       val heavyObject = HeavyObject()
       return { println("Callback") } // Captures heavyObject unnecessarily
   }
   
   // GOOD: Capture only what's needed
   fun createCallback(): () -> Unit {
       val heavyObject = HeavyObject()
       val data = heavyObject.lightData
       return { println("Callback: $data") } // Only captures data
   }
```
1. **Coroutine Management**
``` kotlin
   // Proper coroutine lifecycle
   class MyComponent : CoroutineScope {
       private val job = Job()
       override val coroutineContext = Dispatchers.Main + job
       
       fun cleanup() {
           job.cancel() // Cancel all coroutines
       }
   }
```
### Code Review Checklist
- [ ] Are all listeners properly unregistered?
- [ ] Are all threads and timers properly stopped?
- [ ] Are resources closed in finally blocks or try-with-resources?
- [ ] Do caches have eviction policies?
- [ ] Are static collections cleared when appropriate?
- [ ] Do inner classes need outer class access?
- [ ] Are closures capturing more than necessary?

## 🔍 Debugging Memory Leaks
### Step-by-Step Process
1. **Identify Symptoms**
    - Application slowing down over time
    - OutOfMemoryError exceptions
    - High GC activity

2. **Collect Data**
    - Enable GC logging
    - Take heap dumps at different times
    - Monitor memory usage trends

3. **Analyze Heap Dumps**
    - Use tools like Eclipse MAT, JProfiler
    - Look for objects with unexpected retention
    - Identify reference chains keeping objects alive

4. **Locate Root Cause**
    - Find GC roots holding references
    - Identify patterns in leaked objects
    - Trace back to problematic code

5. **Implement Fix**
    - Apply appropriate prevention strategy
    - Test fix with memory monitoring
    - Verify leak is resolved

### Common Analysis Patterns
- **Large collections in heap dumps**: Static collection leaks
- **Many listener objects**: Observer pattern leaks
- **Thread objects not being collected**: Thread leaks
- **Objects held by inner classes**: Anonymous class leaks
- **Cache objects growing over time**: Cache eviction issues

## 📚 Additional Resources
### Documentation
- [Oracle Java Memory Management](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/)
- [Kotlin Memory Management](https://kotlinlang.org/docs/native-memory-management.html)
- [JVM Garbage Collection](https://www.oracle.com/webfolder/technetwork/tutorials/obe/java/gc01/index.html)

### Tools
- [Eclipse Memory Analyzer (MAT)](https://www.eclipse.org/mat/)
- [JVisualVM](https://visualvm.github.io/)
- [JProfiler](https://www.ej-technologies.com/products/jprofiler/overview.html)
- [YourKit](https://www.yourkit.com/)

### Best Practices
- [Effective Java](https://www.oracle.com/java/technologies/effective-java.html) - Item 7: Eliminate obsolete object references
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Java Memory Management Best Practices](https://stackify.com/java-memory-management-best-practices/)
