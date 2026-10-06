(function() {
'use strict';

const PLUGIN_API_VERSION = '3.0';

let _ready = null;
function whenReady() {
    if (_ready) return _ready;
    _ready = new Promise((resolve) => {
        if (typeof Neutralino !== 'undefined') {
            Neutralino.init();
            Neutralino.events.on('windowClose', () => Neutralino.app.exit());
            Neutralino.app.getConfig().then(() => resolve()).catch(() => resolve());
        } else resolve();
    });
    return _ready;
}

let APP_DIR = '';
async function getAppDir() {
    if (APP_DIR) return APP_DIR;
    const dataPath = await Neutralino.os.getPath('data');
    APP_DIR = dataPath.replace(/\/$/, '') + '/硒瓜计算器';
    const subs = ['plugins','logs'];
    for (const s of subs) {
        try { await Neutralino.filesystem.createDirectory(APP_DIR + '/' + s); } catch(e) {}
    }
    return APP_DIR;
}
async function subDir(name) { return (await getAppDir()) + '/' + name; }

let logFile = '';
async function writeLog(level, source, message) {
    try {
        if (!logFile) {
            const d = new Date();
            const p = n => String(n).padStart(2,'0');
            logFile = (await subDir('logs')) + '/' + d.getFullYear() + '-' + p(d.getMonth()+1) + '-' + p(d.getDate()) + '-UTC' + p(d.getHours()) + '-' + p(d.getMinutes()) + '.log';
        }
        await Neutralino.filesystem.appendFile(logFile, '[' + new Date().toISOString() + '] [' + level + '] [' + source + '] ' + message + '\n');
    } catch(e) {}
}

async function readBinBase64(path) {
    const buf = await Neutralino.filesystem.readBinaryFile(path);
    let bin = '';
    const bytes = new Uint8Array(buf);
    for (let i = 0; i < bytes.byteLength; i++) bin += String.fromCharCode(bytes[i]);
    return btoa(bin);
}

function isImagePath(s) {
    return /\.(png|jpg|jpeg|gif|webp|bmp|ico|svg)$/i.test(s);
}
function getMime(path) {
    const ext = path.split('.').pop().toLowerCase();
    if (ext === 'jpg') return 'jpeg';
    if (ext === 'svg') return 'svg+xml';
    return ext;
}

async function listPlugins() {
    const dir = await subDir('plugins');
    try {
        const entries = await Neutralino.filesystem.readDirectory(dir);
        const dirs = entries.filter(e => e.type === 'DIRECTORY' && !e.entry.startsWith('upload_'));
        const result = [];
        for (const d of dirs) {
            const pd = dir + '/' + d.entry;
            try {
                const setRaw = await Neutralino.filesystem.readFile(pd + '/set.json');
                const meta = JSON.parse(setRaw);
                const apiVer = String(meta.api || '1.0');
                if (apiVer !== PLUGIN_API_VERSION && apiVer !== '3.0') {
                    console.warn('跳过插件（API 版本不匹配 ' + apiVer + '）:', d.entry);
                    continue;
                }
                const html = await Neutralino.filesystem.readFile(pd + '/index.html');

                let readme = '';
                try { readme = await Neutralino.filesystem.readFile(pd + '/README.md'); } catch(e) {}

                let iconEmoji = '🧩';
                let iconImage = '';
                const iconField = (meta.icon || '').trim();

                if (iconField && isImagePath(iconField)) {
                    const relPath = iconField.replace(/^\.\//, '');
                    try {
                        const b64 = await readBinBase64(pd + '/' + relPath);
                        iconImage = 'data:image/' + getMime(relPath) + ';base64,' + b64;
                        iconEmoji = '';
                    } catch(e) {
                        console.warn('读取 icon 失败:', relPath, e.message);
                    }
                } else if (iconField) {
                    iconEmoji = iconField;
                } else {
                    const iconNames = ['icon.png','icon.jpg','icon.jpeg','icon.gif','icon.webp','icon.svg'];
                    for (const n of iconNames) {
                        try {
                            const b64 = await readBinBase64(pd + '/' + n);
                            iconImage = 'data:image/' + getMime(n) + ';base64,' + b64;
                            iconEmoji = '';
                            break;
                        } catch(e) {}
                    }
                }

                result.push({
                    id: meta.id || d.entry,
                    name: meta.name || d.entry,
                    icon: iconEmoji,
                    iconImage: iconImage,
                    version: meta.version || '1.0.0',
                    api: apiVer,
                    author: meta.author || '',
                    description: meta.description || '',
                    themeAware: meta.themeAware !== false,
                    dir: d.entry,
                    content: html,
                    readme: readme || ''
                });
            } catch(e) {
                console.warn('跳过插件（缺少 set.json 或 index.html）:', d.entry, e.message);
            }
        }
        return result;
    } catch(e) { return []; }
}

async function readPluginAssets(pluginDir, html) {
    const dir = await subDir('plugins');
    const base = dir + '/' + pluginDir;
    let result = html;

    const linkRe = /<link\s+[^>]*rel=["']stylesheet["'][^>]*href=["']([^"']+)["'][^>]*\/?>/gi;
    for (const m of [...result.matchAll(linkRe)]) {
        const href = m[1];
        if (/^(https?:|data:|\/\/)/.test(href)) continue;
        try {
            const css = await Neutralino.filesystem.readFile(base + '/' + href);
            result = result.replace(m[0], '<style>\n' + css + '\n</style>');
        } catch(e) {}
    }

    const scriptRe = /<script\s+[^>]*src=["']([^"']+)["'][^>]*><\/script>/gi;
    for (const m of [...result.matchAll(scriptRe)]) {
        const src = m[1];
        if (/^(https?:|data:|\/\/)/.test(src)) continue;
        try {
            const js = await Neutralino.filesystem.readFile(base + '/' + src);
            result = result.replace(m[0], '<script>\n' + js + '\n</script>');
        } catch(e) {}
    }

    const imgRe = /(src|href)=["']([^"']+\.(png|jpg|jpeg|gif|webp|svg|bmp|ico))["']/gi;
    for (const m of [...result.matchAll(imgRe)]) {
        const attr = m[1], path = m[2];
        if (/^(https?:|data:|\/\/)/.test(path)) continue;
        try {
            const b64 = await readBinBase64(base + '/' + path);
            const mime = getMime(path);
            result = result.split(m[0]).join(attr + '="data:image/' + mime + ';base64,' + b64 + '"');
        } catch(e) {}
    }
    return result;
}

async function openDir(sub) {
    try {
        const dir = await subDir(sub);
        await Neutralino.os.open(dir);
    } catch(e) { console.error('打开目录失败:', e); }
}

async function openExternal(url) { await Neutralino.os.open(url); }

async function uploadPlugin(b64, filename) {
    try {
        const pluginsDir = await subDir('plugins');
        const tempName = 'upload_' + Date.now() + '_' + filename;
        const tempPath = pluginsDir + '/' + tempName;

        const binStr = atob(b64);
        const bytes = new Uint8Array(binStr.length);
        for (let i = 0; i < binStr.length; i++) bytes[i] = binStr.charCodeAt(i);
        await Neutralino.filesystem.writeBinaryFile(tempPath, bytes.buffer);

        const extractResult = await extractArchive(tempPath, pluginsDir);
        if (!extractResult.ok) {
            try { await Neutralino.filesystem.removeFile(tempPath); } catch(e) {}
            return { ok: false, error: extractResult.error };
        }
        try { await Neutralino.filesystem.removeFile(tempPath); } catch(e) {}

        const entries = await Neutralino.filesystem.readDirectory(pluginsDir);
        const newDirs = entries.filter(e => e.type === 'DIRECTORY' && !e.entry.startsWith('upload_'));
        let pluginName = '';
        let error = '';

        for (const d of newDirs) {
            const pd = pluginsDir + '/' + d.entry;
            let hasSet = false, hasIndex = false, apiVer = '';

            try {
                const setRaw = await Neutralino.filesystem.readFile(pd + '/set.json');
                const meta = JSON.parse(setRaw);
                hasSet = true;
                apiVer = String(meta.api || '1.0');
                if (apiVer !== PLUGIN_API_VERSION) {
                    error = '插件接口版本不兼容：' + apiVer + '（当前支持 ' + PLUGIN_API_VERSION + '）';
                }
            } catch(e) { hasSet = false; }

            try {
                await Neutralino.filesystem.readFile(pd + '/index.html');
                hasIndex = true;
            } catch(e) { hasIndex = false; }

            if (hasSet && hasIndex && apiVer === PLUGIN_API_VERSION) {
                pluginName = d.entry;
                break;
            } else {
                if (!hasSet) error = error || '缺少 set.json';
                else if (!hasIndex) error = error || '缺少 index.html';
            }
        }

        if (!pluginName) {
            return { ok: false, error: error || '未找到有效的插件目录' };
        }

        return { ok: true, pluginName: pluginName };
    } catch (e) {
        return { ok: false, error: e.message || String(e) };
    }
}

async function extractArchive(archivePath, destDir) {
    const lower = archivePath.toLowerCase();
    const is7z = lower.endsWith('.7z');

    // Linux/macOS：7z / 7za / 7zr
    const sevenTools = ['7z','7za','7zr'];
    for (const tool of sevenTools) {
        try {
            const r = await Neutralino.os.execCommand(tool + ' x "' + archivePath + '" -o"' + destDir + '" -y');
            if (r.exitCode === 0) return { ok: true };
        } catch(e) {}
    }

    // zip 专属
    if (!is7z) {
        // Linux/macOS：unzip
        try {
            const r = await Neutralino.os.execCommand('unzip -o "' + archivePath + '" -d "' + destDir + '"');
            if (r.exitCode === 0) return { ok: true };
        } catch(e) {}

        // Windows：PowerShell
        try {
            const r = await Neutralino.os.execCommand('powershell -NoProfile -Command "Expand-Archive -Path \'' + archivePath + '\' -DestinationPath \'' + destDir + '\' -Force"');
            if (r.exitCode === 0) return { ok: true };
        } catch(e) {}

        // Windows：tar（Win10+ 自带 bsdtar）
        try {
            const r = await Neutralino.os.execCommand('tar -xf "' + archivePath + '" -C "' + destDir + '"');
            if (r.exitCode === 0) return { ok: true };
        } catch(e) {}
    }

    // 7z 无工具时用 tar 尝试（部分 .7z 用 tar 无法处理，会失败）
    try {
        const r = await Neutralino.os.execCommand('tar -xf "' + archivePath + '" -C "' + destDir + '"');
        if (r.exitCode === 0) return { ok: true };
    } catch(e) {}

    return { ok: false, error: '未找到可用的解压工具，请安装 7-Zip 或 unzip' };
}

window.appAPI = {
    listPlugins: function() { return whenReady().then(function() { return listPlugins(); }); },
    readPluginAssets: function(dir, html) { return whenReady().then(function() { return readPluginAssets(dir, html); }); },
    openPluginsDir: function() { return whenReady().then(function() { return openDir('plugins'); }); },
    openLogsDir: function() { return whenReady().then(function() { return openDir('logs'); }); },
    log: function(level, source, message) { return whenReady().then(function() { return writeLog(level, source, message); }); },
    openExternal: function(url) { return whenReady().then(function() { return openExternal(url); }); },
    uploadPlugin: function(b64, filename) { return whenReady().then(function() { return uploadPlugin(b64, filename); }); }
};

whenReady().then(function() {
    writeLog('INFO', 'Main', '应用启动 v26.4.0 · 插件 API ' + PLUGIN_API_VERSION);
    console.log('✅ appAPI 已就绪，插件接口版本 ' + PLUGIN_API_VERSION);
});
})();