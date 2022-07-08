# amap-jsapi-loader
amap-jsapi-loader 是高德开放平台官网提供的地图 JSAPI 的加载器，可帮助开发者快速定位、有效避免加载引用地图 JSAPI 各种错误用法。


该加载器具有以下特性：
* 支持以 普通JS 和 npm包 两种方式使用；
* 有效避免错误异步加载导致的 JSAPI 资源加载不完整问题；
* 对于加载混用多个版本 JSAPI 的错误用法给予报错处理；
* 对于不合法加载引用 JSAPI 给予报错处理；
* 支持指定 JSAPI 版本；
* 支持插件加载；
* 允许多次执行加载操作，网络资源不会重复请求，便于大型工程模块管理；


# USAGE

## AMapLoader.load方法参数说明
```js
AMapLoader.load({
    "key": "",              // 申请好的Web端开发者Key，首次调用 load 时必填
    "version": "2.0",   // 指定要加载的 JSAPI 的版本，缺省时默认为 1.4.15
    "plugins": []           // 需要使用的的插件列表，如比例尺'AMap.Scale'等
    "AMapUI": {             // 是否加载 AMapUI，缺省不加载
        "version": '1.1',   // AMapUI 缺省 1.1
        "plugins":[],       // 需要加载的 AMapUI ui插件
    },
    "Loca":{                // 是否加载 Loca， 缺省不加载
        "version": '1.3.2'  // Loca 版本，缺省 1.3.2
    },
}).then(()=>{
    window.AMap.xx;
    window.AMapUI.xx;
    window.Loca.xx
});

```

## AMapUI

AMapUI 用法和官网有一点点区别。通过 AMapUI.xx 来获取组件
```js
AMapLoader.load({
    key: '',//首次load必填
    version: '2.0',
    AMapUI: {
        version: '1.1',
        plugins: ['overlay/SimpleMarker'],
    }
}).then((AMap) => {
    map = new AMap.Map('container');

    // !!! 通过 AMap.SimpleMarker 获取组件
    new AMapUI.SimpleMarker({
        //前景文字
        iconLabel: 'A',
        //图标主题
        iconTheme: 'default',
        //背景图标样式
        iconStyle: 'red',
        map: map,
        position: map.getCenter()
    });
}).catch((e) => {
    console.error(e);
});
```


# 使用
#### 以普通 JS 方式使用 Loader
尚未发布在线Loader，可将 dist/index.js 复制到项目下
```
<script src="../dist/index.js"></script>
<script>
    AMapLoader.load({
        key:'',//首次load必填
        version:'2.0',
        plugins:['AMap.Scale']
    }).then((AMap)=>{
        map = new AMap.Map('container');
        map.addControl(new AMap.Scale())
    }).catch((e)=>{
        console.error(e);
    });   
</script>
```
#### 以 NPM 包方式使用 Loader
安装
```
tnpm i @alife/amap-jsapi-loader --save-dev
```
使用
```
import AMapLoader from '@alife/amap-jsapi-loader';

AMapLoader.load().then((AMap)=>{
    map = new AMap.Map('container');
}).catch(e=>{
    console.log(e);
})

```

# 相关链接：
地&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;图 JSAPI：  &nbsp;&nbsp;[示例中心](https://lbs.amap.com/demo-center/js-api)&nbsp;&nbsp;&nbsp;&nbsp;[教程](https://lbs.amap.com/api/javascript-api/summary)&nbsp;&nbsp;&nbsp;&nbsp;[参考手册](https://lbs.amap.com/api/javascript-api/reference/map)



数据可视化 JSAPI：  &nbsp;&nbsp;[示例中心](https://lbs.amap.com/demo-center/loca-api)&nbsp;&nbsp;&nbsp;&nbsp;[教程](https://lbs.amap.com/api/loca-api/prod_intro)&nbsp;&nbsp;&nbsp;&nbsp;[参考手册](https://lbs.amap.com/api/loca-api/guide/baselayer)


