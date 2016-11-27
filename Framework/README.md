# Setting your own Framework-based Bot
- Change `src/main/resources/root.miyuki` to the root of your bot (You can leave it blank to search all but it'll be laggy to start)
- Based on your root you've set up, create classes with `@Module`

Example Class:
```java
package br.com.brjdevs.miyuki.modules.init;

import br.com.brjdevs.miyuki.framework.Module;

@Module(id = "example", order = 0)
public class ExampleModule {
    // Content
}
```

##  Annotation Reference:
### Classes
#### @Module
```java
@Module {
    String id();
    String name() default "";
    Type[] type() default {Type.STATIC};
    boolean isListener() default false;int order();
}
```
Use this to declare the class as a module to be loaded by the **LoadController**. When defined, the annotations below start working.

### Fields
#### @LoggerInstance
```java
@LoggerInstance {}
```
This creates a SLF4J Logger for the Module with the `Logger.getName() = @Module.name`

#### @SelfUserInstance
```java
@SelfUserInstance {}
```
This is setted when the JDA sends `ReadyEvent`. It's a shortcut instead of `JDA#getSelfUser` and it'll being kept updated in case of Cache reload.
#### @Container
```java
@Container {}
```
This will set the field to the `br.com.brjdevs.miyuki.framework.entities.ModuleContainer` of your class. 

#### @JDAInstance
```java
@JDAInstance {}
```
This is setted when the JDA sends `ReadyEvent`, and it's the `JDA` object itself.

### @Instance
```java
@Instance {}
```

```java
@Resource {
    String value();
}
```
```java
@ResourceManager {}
```
```java
@JSONResource {
    String value();
}
```

### Methods
```java
@Command {
    String value();
}
```
```java
@CommandRegister {}
```
```java
@Predicate {}
```
```java
@OnEnabled {}
```
```java
@OnDisabled {}
```
```java
@PreReady {}
```
```java
@Ready {}
```
```java
@PostReady {}
```