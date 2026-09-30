/* =========================================================
   硒瓜计算器 · Neutralino API 封装
   ========================================================= */

(function() {
    'use strict';

    let _ready = null;
    function whenReady() {
        if (_ready) return _ready;
        _ready = new Promise((resolve) => {
            if (typeof Neutralino !== 'undefined') {
                Neutralino.init();
                Neutralino.events.on('windowClose', () => Neutralino.app.exit());
                Neutralino.app.getConfig().then(() => resolve()).catch(() => resolve());
            } else {
                resolve();
            }
        });
        return _ready;
    }

    let APP_DIR = '';
    async function getAppDir() {
        if (APP_DIR) return APP_DIR;
        const dataPath = await Neutralino.os.getPath('data');
        APP_DIR = dataPath.replace(/\/$/, '') + '/硒瓜计算器';
        const subs = ['plugins', 'shaders', 'backgrounds', 'languages', 'logs'];
        for (const s of subs) {
            try { await Neutralino.filesystem.createDirectory(APP_DIR + '/' + s); } catch (e) {}
        }
        return APP_DIR;
    }
    async function subDir(name) { return (await getAppDir()) + '/' + name; }

    let logFile = '';
    async function writeLog(level, source, message) {
        try {
            if (!logFile) {
                const d = new Date();
                const pad = n => String(n).padStart(2, '0');
                logFile = (await subDir('logs')) +
                    `/${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())}-UTC${pad(d.getHours())}-${pad(d.getMinutes())}.log`;
            }
            const line = `[${new Date().toISOString()}] [${level}] [${source}] ${message}\n`;
            await Neutralino.filesystem.appendFile(logFile, line);
        } catch (e) {}
    }

    async function listPlugins() {
        const dir = await subDir('plugins');
        try {
            const entries = await Neutralino.filesystem.readDirectory(dir);
            const files = entries.filter(e => e.type === 'FILE' && e.entry.endsWith('.html'));
            const result = [];
            for (const f of files) {
                try {
                    const html = await Neutralino.filesystem.readFile(dir + '/' + f.entry);
                    const meta = {};
                    const re = /<meta\s+name=["']plugin-([\w-]+)["']\s+content=["']([^"']*)["']\s*\/?>/gi;
                    let m;
                    while ((m = re.exec(html)) !== null) meta[m[1]] = m[2];
                    result.push({
                        id: meta.id || f.entry.replace('.html', ''),
                        name: meta.name || f.entry,
                        icon: meta.icon || '🧩',
                        version: meta.version || '1.0.0',
                        author: meta.author || '',
                        description: meta.description || '',
                        themeAware: meta['theme-aware'] === 'true',
                        file: f.entry,
                        content: html,
                    });
                } catch (e) {}
            }
            return result;
        } catch (e) { return []; }
    }

    async function listShaders() {
        const dir = await subDir('shaders');
        try {
            const entries = await Neutralino.filesystem.readDirectory(dir);
            const files = entries.filter(e => e.type === 'FILE' && e.entry.endsWith('.css'));
            const result = [];
            for (const f of files) {
                try {
                    const css = await Neutralino.filesystem.readFile(dir + '/' + f.entry);
                    const m = css.match(/\/\*\s*@shader\s*([\s\S]*?)\*\//);
                    if (!m) continue;
                    const meta = JSON.parse(m[1].trim());
                    result.push({
                        id: meta.id,
                        name: meta.name || meta.id,
                        icon: meta.icon || '✨',
                        mode: meta.mode || 'both',
                        file: f.entry,
                        css,
                    });
                } catch (e) {}
            }
            return result;
        } catch (e) { return []; }
    }

    async function listBackgrounds() {
        const dir = await subDir('backgrounds');
        const exts = ['.jpg', '.jpeg', '.png', '.webp', '.gif', '.bmp'];
        try {
            const entries = await Neutralino.filesystem.readDirectory(dir);
            return entries
                .filter(e => e.type === 'FILE' && exts.some(x => e.entry.toLowerCase().endsWith(x)))
                .map(e => ({ file: e.entry, name: e.entry.replace(/\.[^.]+$/, '') }));
        } catch (e) { return []; }
    }

    async function readBackground(file) {
        const dir = await subDir('backgrounds');
        const buf = await Neutralino.filesystem.readBinaryFile(dir + '/' + file);
        const ext = file.split('.').pop().toLowerCase();
        const mime = ext === 'jpg' ? 'jpeg' : ext;
        let binary = '';
        const bytes = new Uint8Array(buf);
        for (let i = 0; i < bytes.byteLength; i++) binary += String.fromCharCode(bytes[i]);
        return `data:image/${mime};base64,${btoa(binary)}`;
    }

    async function listLanguages() {
        const dir = await subDir('languages');
        try {
            const entries = await Neutralino.filesystem.readDirectory(dir);
            const files = entries.filter(e => e.type === 'FILE' && e.entry.endsWith('.json') && !e.entry.startsWith('_'));
            const result = [];
            for (const f of files) {
                try {
                    const raw = await Neutralino.filesystem.readFile(dir + '/' + f.entry);
                    const obj = JSON.parse(raw);
                    if (!obj.id || !obj.name) continue;
                    result.push({ ...obj, file: f.entry });
                } catch (e) {}
            }
            return result;
        } catch (e) { return []; }
    }

    async function openDir(sub) {
        const dir = await subDir(sub);
        await Neutralino.os.open(dir);
    }

    async function openExternal(url) { await Neutralino.os.open(url); }

    window.appAPI = {
        listPlugins:      () => whenReady().then(() => listPlugins()),
        listShaders:      () => whenReady().then(() => listShaders()),
        listBackgrounds:  () => whenReady().then(() => listBackgrounds()),
        listLanguages:    () => whenReady().then(() => listLanguages()),
        readBackground:   (f) => whenReady().then(() => readBackground(f)),
        openPluginsDir:   () => whenReady().then(() => openDir('plugins')),
        openShadersDir:   () => whenReady().then(() => openDir('shaders')),
        openBackgroundsDir: () => whenReady().then(() => openDir('backgrounds')),
        openLanguagesDir: () => whenReady().then(() => openDir('languages')),
        openLogsDir:      () => whenReady().then(() => openDir('logs')),
        log:              (level, source, message) => whenReady().then(() => writeLog(level, source, message)),
        openExternal:     (url) => whenReady().then(() => openExternal(url)),
    };

    whenReady().then(() => writeLog('INFO', 'Main', '应用启动'));
})();