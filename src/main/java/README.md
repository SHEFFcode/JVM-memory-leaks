## **Memory Leak Types Covered:**
### 1. **Static Collection Leak**
Objects stored in static collections persist for the application lifetime.
### 2. **Listener/Observer Pattern Leak**
Components register as listeners but forget to unregister, keeping them alive.
### 3. **Thread Leak**
Non-daemon threads prevent JVM shutdown and keep objects alive.
### 4. **Anonymous Inner Class Leak**
Anonymous classes hold implicit references to outer class instances.
### 5. **Cache without Eviction**
Unbounded caches grow indefinitely without cleanup policies.
### 6. **Unclosed Resources Leak**
File handles, timers, and other resources not properly closed.
### 7. **HashCode/Equals Contract Violation**
Objects stuck in collections due to modified hash codes.
### 8. **Proper Resource Management**
Shows correct usage of try-with-resources and AutoCloseable.
## **Key Java-Specific Memory Leak Patterns:**
1. **Inner class references** - Anonymous and non-static inner classes hold references to outer instances
2. **Thread management** - Non-daemon threads prevent JVM shutdown
3. **Resource management** - Files, streams, database connections not closed
4. **Collection mutations** - Modifying objects used as keys in HashMap/HashSet
5. **Static references** - Long-lived static collections

## **Prevention Strategies:**
1. **Use try-with-resources** for AutoCloseable resources
2. **Always unregister listeners** before discarding objects
3. **Use WeakReference/WeakHashMap** when appropriate
4. **Make inner classes static** when they don't need outer class access
5. **Implement proper cleanup methods** and call them consistently
6. **Use daemon threads** for background tasks
7. **Never modify objects** after adding them as keys to hash-based collections
