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