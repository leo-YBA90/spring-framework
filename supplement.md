####enclose class
````
public class A {
    public class B {
    
    }
};
````
需要实例B类时,按照正逻辑是,A.B ab = new A.B();
那么编译器就会出现一个错误–“is not an enclosing class”
再翻看相关的java代码,发现原来写法出错了!正确的做法是
A a = new A();
A.B ab = a.new B();

没有静态(static)的类中类不能使用外部类进行.操作,必须用实例来进行实例化类中类.

####java原生注解
#####@FunctionalInterface
函数式接口，在这个接口里面只能有一个抽象方法。  
它们主要用在Lambda表达式和方法引用（实际上也可认为是Lambda表达式）上。主要用于编译级错误检查，加上该注解，当你写的接口不符合函数式接口定义的时候，编译器会报错。
````
@FunctionalInterface  
    interface GreetingService 
    {
        void sayMessage(String message);
    }
````

那么就可以使用Lambda表达式来表示该接口的一个实现(注：JAVA 8 之前一般是用匿名类实现的)：
````
GreetingService greetService1 = message -> System.out.println("Hello " + message);
````

####注解
注解@Order或者接口Ordered的作用是定义Spring IOC容器中Bean的执行顺序的优先级，而不是定义Bean的加载顺序，Bean的加载顺序不受@Order或Ordered接口的影响；  
1. 注解可以作用在类(接口、枚举)、方法、字段声明（包括枚举常量）；
2. 注解有一个int类型的参数，可以不传，默认是最低优先级；  
3. 通过常量类的值我们可以推测参数值越小优先级越高；

####注解合成
java可以使用注解对原生注解或spring注解进行合成  
例如：
````
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping
public @interface PathRestController {
    @AliasFor("path")
    String[] value() default {};
    
    @AliasFor("value")
    String[] path() default {};
}
````