# ReactiveLite
&emsp;&emsp;一个轻量级的Android响应式编程库，如果你想使用类似RxJava(RxAndroid)的响应式编程，但是又觉得其体积臃肿、操作符过多或者学习成本高的话，本库十分适合你。

&emsp;&emsp;另外：基于此库开发出轻量级的事件总线[TinyBus](https://github.com/maxwell-nc/TinyBus)

## 特点 Feature

- 多线程调度（主线程/子线程/并发多线程）
- 体积小/方法数量少（约30kb/150）

## 依赖 Gradle

目前部署1.3版本部署在JCenter，直接修改build.gradle添加引用：
```
dependencies {
     compile 'com.maxwell.nc:ReactiveLite:1.3'
}
```


## 用法 Usage

&emsp;&emsp;下面以读取图片并显示为场景，其代码如下（Lambda写法）：
```java
Publisher.just("test.png")
         .map((Function<String, Bitmap>) s -> {
             InputStream inputStream = getResources().getAssets().open(s);
             Bitmap image = BitmapFactory.decodeStream(inputStream);
             inputStream.close();
             return image;
         })
         .subscribeOn(Schedulers.newThread())
         .observeOn(Schedulers.uiThread())
         .subscribe(bitmap -> ivImg.setImageBitmap(bitmap));
```

## 支持的操作符

&emsp;&emsp;具体内容请参考 [Wiki部分](https://github.com/maxwell-nc/ReactiveLite/wiki) 

## 进阶 Advance

&emsp;&emsp;更多操作符的用法，可以参考源码中的单元测试部分，所有的操作符都写了对应的单元测试方法并且测试通过。
