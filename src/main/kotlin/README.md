This comprehensive example demonstrates the most common types of memory leaks in Kotlin:
## 1. **Static Collection Leak**
Objects added to static collections are never garbage collected because static references persist for the application lifetime.
## 2. **Listener/Callback Leak**
When objects register as listeners but forget to unregister, they remain in memory even after they're no longer needed.
## 3. **Thread Leak**
Background threads that aren't properly stopped continue running and hold references to objects.
## 4. **Closure Capture Leak**
Lambdas and closures capture their entire lexical scope, potentially holding onto large objects unnecessarily.
## 5. **Cache without Eviction**
Caches that grow indefinitely without cleanup policies consume more and more memory.
## **Prevention Tips:**
1. **Use weak references** when appropriate
2. **Always unregister listeners** before clearing objects
3. **Stop threads and timers** explicitly
4. **Be careful with closures** - only capture what you need
5. **Implement proper cache eviction policies**
6. **Use `AutoCloseable` and blocks`use`** for resource management
7. **Use memory profilers** to detect leaks early
