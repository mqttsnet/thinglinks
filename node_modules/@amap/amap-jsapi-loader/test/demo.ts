import AMapLoader from "../";
// import {  } from "@ali/test-amap-jsapi";

AMapLoader.load({
  key: "test",
  version: "2.0"
}).then(() => {
  new AMap.Circle({})
  const map = new AMap.Map("div");
  map.on("complete", () => {
    const circle = new AMap.Circle({
      center: [135, 45],
      radius: 40
    });
    map.add(circle);
  });
});
