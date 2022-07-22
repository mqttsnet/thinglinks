if (!window) {
    throw Error("AMap JSAPI can only be used in Browser.");
}
enum LoadStatus {
    notload = "notload",
    loading = "loading",
    loaded = "loaded",
    failed = "failed",
}
let config = {
    key: "",
    AMap: {
        version: "1.4.15",
        plugins: [],
    },
    AMapUI: {
        version: "1.1",
        plugins: [],
    },
    Loca: {
        version: "1.3.2",
    },
};

let Status = {
    AMap: LoadStatus.notload,
    AMapUI: LoadStatus.notload,
    Loca: LoadStatus.notload,
};
let Callback = {
    AMap: [],
    AMapUI: [],
    Loca: [],
};

let onloadCBKs = [];
const onload = function (callback) {
    if (typeof callback == "function") {
        if (Status.AMap === LoadStatus.loaded) {
            callback(window.AMap);
            return;
        }
        onloadCBKs.push(callback);
    }
};

interface LoadOption {
    key: string;
    version?: string;
    plugins?: string[];
    AMapUI?: {
        version?: string;
        plugins?: string[];
    };
    Loca?: {
        version?: string;
    };
}
function appendOther(option: LoadOption): Promise<any> {
    let pros: Promise<void>[] = [];
    if (option.AMapUI) {
        pros.push(loadAMapUI(option.AMapUI));
    }
    if (option.Loca) {
        pros.push(loadLoca(option.Loca));
    }
    return Promise.all(pros);
}
function loadAMapUI(params: { version?: string; plugins?: string[] }): Promise<void> {
    return new Promise((res, rej) => {
        const newPlugins: string[] = [];
        if (params.plugins) {
            for (var i = 0; i < params.plugins.length; i += 1) {
                if (config.AMapUI.plugins.indexOf(params.plugins[i]) == -1) {
                    newPlugins.push(params.plugins[i]);
                }
            }
        }
        if (Status.AMapUI === LoadStatus.failed) {
            rej("前次请求 AMapUI 失败");
        } else if (Status.AMapUI === LoadStatus.notload) {
            Status.AMapUI = LoadStatus.loading;
            config.AMapUI.version = params.version || config.AMapUI.version;
            const version = config.AMapUI.version;
            const parentNode = document.body || document.head;
            const script = document.createElement("script");
            script.type = "text/javascript";
            script.src = `https://webapi.amap.com/ui/${version}/main.js`;

            script.onerror = (e) => {
                Status.AMapUI = LoadStatus.failed;
                rej("请求 AMapUI 失败");
            };
            script.onload = () => {
                Status.AMapUI = LoadStatus.loaded;
                if (newPlugins.length) {
                    window.AMapUI.loadUI(newPlugins, function () {
                        for (let i = 0, len = newPlugins.length; i < len; i++) {
                            const path = newPlugins[i];
                            const name = path.split("/").slice(-1)[0];
                            window.AMapUI[name] = arguments[i];
                        }
                        res();
                        while (Callback.AMapUI.length) {
                            Callback.AMapUI.splice(0, 1)[0]();
                        }
                    });
                } else {
                    res();
                    while (Callback.AMapUI.length) {
                        Callback.AMapUI.splice(0, 1)[0]();
                    }
                }
            };
            parentNode.appendChild(script);
        } else if (Status.AMapUI === LoadStatus.loaded) {
            if (params.version && params.version !== config.AMapUI.version) {
                rej("不允许多个版本 AMapUI 混用");
            } else {
                if (newPlugins.length) {
                    window.AMapUI.loadUI(newPlugins, function () {
                        for (let i = 0, len = newPlugins.length; i < len; i++) {
                            const path = newPlugins[i];
                            const name = path.split("/").slice(-1)[0];
                            window.AMapUI[name] = arguments[i];
                        }
                        res();
                    });
                } else {
                    res();
                }
            }
        } else {
            if (params.version && params.version !== config.AMapUI.version) {
                rej("不允许多个版本 AMapUI 混用");
            } else {
                Callback.AMapUI.push((err) => {
                    if (err) {
                        rej(err);
                    } else {
                        if (newPlugins.length) {
                            window.AMapUI.loadUI(newPlugins, function () {
                                for (let i = 0, len = newPlugins.length; i < len; i++) {
                                    const path = newPlugins[i];
                                    const name = path.split("/").slice(-1)[0];
                                    window.AMapUI[name] = arguments[i];
                                }
                                res();
                            });
                        } else {
                            res();
                        }
                    }
                });
            }
        }
    });
}

function loadLoca(params: { version?: string }): Promise<void> {
    return new Promise((res, rej) => {
        if (Status.Loca === LoadStatus.failed) {
            rej("前次请求 Loca 失败");
        } else if (Status.Loca === LoadStatus.notload) {
            Status.Loca = LoadStatus.loading;
            config.Loca.version = params.version || config.Loca.version;
            const version = config.Loca.version;
            const isApiV2 = config.AMap.version.startsWith("2");
            const isLocaV2 = version.startsWith("2");
            if ((isApiV2 && !isLocaV2) || (!isApiV2 && isLocaV2)) {
                rej("JSAPI 与 Loca 版本不对应！！");
                return;
            }
            const key = config.key;
            const parentNode = document.body || document.head;
            const script = document.createElement("script");
            script.type = "text/javascript";
            script.src = `https://webapi.amap.com/loca?v=${version}&key=${key}`;

            script.onerror = (e) => {
                Status.Loca = LoadStatus.failed;
                rej("请求 AMapUI 失败");
            };
            script.onload = () => {
                Status.Loca = LoadStatus.loaded;
                res();
                while (Callback.Loca.length) {
                    Callback.Loca.splice(0, 1)[0]();
                }
            };
            parentNode.appendChild(script);
        } else if (Status.Loca === LoadStatus.loaded) {
            if (params.version && params.version !== config.Loca.version) {
                rej("不允许多个版本 Loca 混用");
            } else {
                res();
            }
        } else {
            if (params.version && params.version !== config.Loca.version) {
                rej("不允许多个版本 Loca 混用");
            } else {
                Callback.Loca.push((err) => {
                    if (err) {
                        rej(err);
                    } else {
                        rej();
                    }
                });
            }
        }
    });
}

const load = function (options: LoadOption) {
    return new Promise((resolve, reject) => {
        if (Status.AMap == LoadStatus.failed) {
            reject("");
        } else if (Status.AMap == LoadStatus.notload) {
            //初次加载
            let { key, version, plugins } = options;
            if (!key) {
                reject("请填写key");
                return;
            }
            if (window.AMap && location.host !== "lbs.amap.com") {
                reject("禁止多种API加载方式混用");
            }
            config.key = key;
            config.AMap.version = version || config.AMap.version;
            config.AMap.plugins = plugins || config.AMap.plugins;
            Status.AMap = LoadStatus.loading;

            const parentNode = document.body || document.head;

            window.___onAPILoaded = function (err) {
                delete window.___onAPILoaded;
                if (err) {
                    Status.AMap = LoadStatus.failed;
                    reject(err);
                } else {
                    Status.AMap = LoadStatus.loaded;
                    appendOther(options)
                        .then(() => {
                            resolve(window.AMap);
                        })
                        .catch(reject);
                    while (onloadCBKs.length) {
                        onloadCBKs.splice(0, 1)[0]();
                    }
                }
            };
            const script = document.createElement("script");
            script.type = "text/javascript";

            script.src =
                "https://webapi.amap.com/maps?callback=___onAPILoaded&v=" +
                config.AMap.version +
                "&key=" +
                key +
                "&plugin=" +
                config.AMap.plugins.join(",");
            script.onerror = (e) => {
                Status.AMap = LoadStatus.failed;
                reject(e);
            };
            parentNode.appendChild(script);
        } else if (Status.AMap == LoadStatus.loaded) {
            //deal multi load
            if (options.key && options.key !== config.key) {
                reject("多个不一致的 key");
                return;
            }
            if (options.version && options.version !== config.AMap.version) {
                reject("不允许多个版本 JSAPI 混用");
                return;
            }
            const newPlugins = [];
            if (options.plugins) {
                for (var i = 0; i < options.plugins.length; i += 1) {
                    if (config.AMap.plugins.indexOf(options.plugins[i]) == -1) {
                        newPlugins.push(options.plugins[i]);
                    }
                }
            }
            if (newPlugins.length) {
                window.AMap.plugin(newPlugins, () => {
                    appendOther(options)
                        .then(() => {
                            resolve(window.AMap);
                        })
                        .catch(reject);
                });
            } else {
                appendOther(options)
                    .then(() => {
                        resolve(window.AMap);
                    })
                    .catch(reject);
            }
        } else {
            // loading
            if (options.key && options.key !== config.key) {
                reject("多个不一致的 key");
                return;
            }
            if (options.version && options.version !== config.AMap.version) {
                reject("不允许多个版本 JSAPI 混用");
                return;
            }
            const newPlugins = [];
            if (options.plugins) {
                for (var i = 0; i < options.plugins.length; i += 1) {
                    if (config.AMap.plugins.indexOf(options.plugins[i]) == -1) {
                        newPlugins.push(options.plugins[i]);
                    }
                }
            }
            onload(() => {
                if (newPlugins.length) {
                    window.AMap.plugin(newPlugins, () => {
                        appendOther(options)
                            .then(() => {
                                resolve(window.AMap);
                            })
                            .catch(reject);
                    });
                } else {
                    appendOther(options)
                        .then(() => {
                            resolve(window.AMap);
                        })
                        .catch(reject);
                }
            });
        }
    });
};
function reset() {
    delete window.AMap;
    delete window.AMapUI;
    delete window.Loca;
    config = {
        key: "",
        AMap: {
            version: "1.4.15",
            plugins: [],
        },
        AMapUI: {
            version: "1.1",
            plugins: [],
        },
        Loca: {
            version: "1.3.2",
        },
    };
    Status = {
        AMap: LoadStatus.notload,
        AMapUI: LoadStatus.notload,
        Loca: LoadStatus.notload,
    };
    Callback = {
        AMap: [],
        AMapUI: [],
        Loca: [],
    };
}
export default { load, reset };
