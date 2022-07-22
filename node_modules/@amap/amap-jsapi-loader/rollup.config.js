import server from "rollup-plugin-server";
import babel from "rollup-plugin-babel";
import compiler from "@ampproject/rollup-plugin-closure-compiler";
import rollupTypescript from 'rollup-plugin-typescript2';

export default {
    input: "src/index.ts",
    output: {
        file: "dist/index.js",
        format: "umd",
        name: "AMapLoader",
    },
    plugins: [
        rollupTypescript(),
        babel({
            presets: [["@babel/env", { targets: { ie: 9 } }]],
        }),
        compiler(),
        server({
            contentBase: "./",
            host: "127.0.0.1",
            port: 3601,
        }),
    ],
};
