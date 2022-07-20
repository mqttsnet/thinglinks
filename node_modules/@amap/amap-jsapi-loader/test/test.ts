import AMapLoader from "../";
import "@amap/amap-jsapi-loader";

AMapLoader.load({
  key: "test",
  version: "2.0"
}).then(() => {});
