# 概念
在 Java 中 `Class.forName()` 和 `ClassLoader` 都可以对类进行加载。ClassLoader 就是遵循双亲委派模型最终调用启动类加载器的类加载器，实现的功能是 `通过一个类的全限定名来获取描述此类的二进制字节流`，获取到二进制流后放到JVM中。`Class.forName()` 方法实际上也是调用的CLassLoader来实现的。

Class.forName(String className); 方法的源码是:
```java
/**
 * Returns the {@code Class} object associated with the class or
 * interface with the given string name.  Invoking this method is
 * equivalent to:
 *
 * <blockquote>
 *  {@code Class.forName(className, true, currentLoader)}
 * </blockquote>
 *
 * where {@code currentLoader} denotes the defining class loader of
 * the current class.
 *
 * <p> For example, the following code fragment returns the
 * runtime {@code Class} descriptor for the class named
 * {@code java.lang.Thread}:
 *
 * <blockquote>
 *   {@code Class t = Class.forName("java.lang.Thread")}
 * </blockquote>
 * <p>
 * A call to {@code forName("X")} causes the class named
 * {@code X} to be initialized.
 *
 * @param      className   the fully qualified name of the desired class.
 * @return     the {@code Class} object for the class with the
 *             specified name.
 * @exception LinkageError if the linkage fails
 * @exception ExceptionInInitializerError if the initialization provoked
 *            by this method fails
 * @exception ClassNotFoundException if the class cannot be located
 */
@CallerSensitive
public static Class<?> forName(String className)
            throws ClassNotFoundException {
    Class<?> caller = Reflection.getCallerClass();
    return forName0(className, true, ClassLoader.getClassLoader(caller), caller);
}
```

最后调用的方法是 `forName0()` 这个方法，在这个 `forName0()` 方法中的第二个参数被默认设置为了true，这个参数代表是否对加载的类进行初始化，设置为true时会类进行初始化，代表会执行类中的静态代码块，以及对静态变量的赋值等操作。

也可以调用 `Class.forName(String name, boolean initialize,ClassLoader loader)` 方法来手动选择在加载类的时候是否要对类进行初始化,的源码如下：

```java
/**
 * Returns the {@code Class} object associated with the class or
 * interface with the given string name, using the given class loader.
 * Given the fully qualified name for a class or interface (in the same
 * format returned by {@code getName}) this method attempts to
 * locate, load, and link the class or interface.  The specified class
 * loader is used to load the class or interface.  If the parameter
 * {@code loader} is null, the class is loaded through the bootstrap
 * class loader.  The class is initialized only if the
 * {@code initialize} parameter is {@code true} and if it has
 * not been initialized earlier.
 *
 * <p> If {@code name} denotes a primitive type or void, an attempt
 * will be made to locate a user-defined class in the unnamed package whose
 * name is {@code name}. Therefore, this method cannot be used to
 * obtain any of the {@code Class} objects representing primitive
 * types or void.
 *
 * <p> If {@code name} denotes an array class, the component type of
 * the array class is loaded but not initialized.
 *
 * <p> For example, in an instance method the expression:
 *
 * <blockquote>
 *  {@code Class.forName("Foo")}
 * </blockquote>
 *
 * is equivalent to:
 *
 * <blockquote>
 *  {@code Class.forName("Foo", true, this.getClass().getClassLoader())}
 * </blockquote>
 *
 * Note that this method throws errors related to loading, linking or
 * initializing as specified in Sections 12.2, 12.3 and 12.4 of <em>The
 * Java Language Specification</em>.
 * Note that this method does not check whether the requested class
 * is accessible to its caller.
 *
 * <p> If the {@code loader} is {@code null}, and a security
 * manager is present, and the caller's class loader is not null, then this
 * method calls the security manager's {@code checkPermission} method
 * with a {@code RuntimePermission("getClassLoader")} permission to
 * ensure it's ok to access the bootstrap class loader.
 *
 * @param name       fully qualified name of the desired class
 * @param initialize if {@code true} the class will be initialized.
 *                   See Section 12.4 of <em>The Java Language Specification</em>.
 * @param loader     class loader from which the class must be loaded
 * @return           class object representing the desired class
 *
 * @exception LinkageError if the linkage fails
 * @exception ExceptionInInitializerError if the initialization provoked
 *            by this method fails
 * @exception ClassNotFoundException if the class cannot be located by
 *            the specified class loader
 *
 * @see       java.lang.Class#forName(String)
 * @see       java.lang.ClassLoader
 * @since     1.2
 */
@CallerSensitive
public static Class<?> forName(String name, boolean initialize,
                               ClassLoader loader)
    throws ClassNotFoundException
{
    Class<?> caller = null;
    SecurityManager sm = System.getSecurityManager();
    if (sm != null) {
        // Reflective call to get caller class is only needed if a security manager
        // is present.  Avoid the overhead of making this call otherwise.
        caller = Reflection.getCallerClass();
        if (sun.misc.VM.isSystemDomainLoader(loader)) {
            ClassLoader ccl = ClassLoader.getClassLoader(caller);
            if (!sun.misc.VM.isSystemDomainLoader(ccl)) {
                sm.checkPermission(
                    SecurityConstants.GET_CLASSLOADER_PERMISSION);
            }
        }
    }
    return forName0(name, initialize, loader, caller);
}
```

其中对参数 initialize 的描述是：
> if {@code true} the class will be initialized.意思就是说：如果参数为true，则加载的类将会被初始化。

# 2. 应用场景
在我们熟悉的Spring框架中的 `IOC` 的实现就是使用的 ClassLoader 。

而在我们使用JDBC时通常是使用 `Class.forName()` 方法来加载数据库连接驱动。这是因为在JDBC规范中明确要求Driver(数据库驱动)类必须向DriverManager注册自己。

