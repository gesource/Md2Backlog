// ブラウザ向けビルドで Node.js 専用モジュール (process, fs) が
// require() されても解決できないエラーを防ぐ。
// NodeMain.kt の runNodeCli() は isNode チェックで実行時ガードされているため
// ブラウザでは呼ばれない。
config.resolve = config.resolve || {};
config.resolve.fallback = Object.assign(config.resolve.fallback || {}, {
    "process": false,
    "fs": false
});
