// ===== 打字机 =====
(function typewriter() {
  const el = document.getElementById('typed-text');
  if (!el) return;
  const text = '高速联合 · 开源 · 共创';
  let i = 0;
  function type() {
    if (i < text.length) {
      el.textContent += text.charAt(i);
      i++;
      setTimeout(type, 80 + Math.random() * 40);
    }
  }
  setTimeout(type, 800);
})();

// ===== 粒子星空 =====
(function particles() {
  const canvas = document.getElementById('particles');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  let w, h, dots = [];
  const COUNT = 80;

  function resize() {
    w = canvas.width = window.innerWidth;
    h = canvas.height = window.innerHeight;
  }
  function init() {
    dots = [];
    for (let i = 0; i < COUNT; i++) {
      dots.push({
        x: Math.random() * w, y: Math.random() * h,
        r: Math.random() * 1.6 + 0.4,
        vx: (Math.random() - 0.5) * 0.25,
        vy: (Math.random() - 0.5) * 0.25,
        a: Math.random() * 0.5 + 0.15,
        pulse: Math.random() * Math.PI * 2
      });
    }
  }
  function draw() {
    ctx.clearRect(0, 0, w, h);
    for (const d of dots) {
      d.x += d.vx; d.y += d.vy;
      if (d.x < 0) d.x = w; if (d.x > w) d.x = 0;
      if (d.y < 0) d.y = h; if (d.y > h) d.y = 0;
      d.pulse += 0.02;
      const alpha = d.a + Math.sin(d.pulse) * 0.15;
      const grad = ctx.createRadialGradient(d.x, d.y, 0, d.x, d.y, d.r * 6);
      grad.addColorStop(0, `rgba(0, 229, 255, ${alpha * 0.8})`);
      grad.addColorStop(1, `rgba(0, 229, 255, 0)`);
      ctx.beginPath(); ctx.arc(d.x, d.y, d.r * 6, 0, Math.PI * 2);
      ctx.fillStyle = grad; ctx.fill();
      ctx.beginPath(); ctx.arc(d.x, d.y, d.r, 0, Math.PI * 2);
      ctx.fillStyle = `rgba(255, 255, 255, ${alpha})`; ctx.fill();
    }
    requestAnimationFrame(draw);
  }
  window.addEventListener('resize', () => { resize(); init(); });
  resize(); init(); draw();
})();

// ===== 区域切换 + 智能推荐（仅下载页） =====
(function regionSwitch() {
  const tabs = document.querySelectorAll('.region-tab');
  if (!tabs.length) return;
  const panels = {
    cn: document.getElementById('panel-cn'),
    intl: document.getElementById('panel-intl')
  };
  const tipText = document.getElementById('auto-tip-text');
  if (!panels.cn || !panels.intl || !tipText) return;

  function switchTo(region) {
    tabs.forEach(t => t.classList.toggle('active', t.dataset.region === region));
    Object.keys(panels).forEach(k => panels[k].classList.toggle('active', k === region));
    tipText.textContent = region === 'cn'
      ? '已为您推荐国内 GitLink 节点，下载速度更快'
      : '您正在使用 GitHub 国际节点，全球 CDN 加速';
  }

  tabs.forEach(tab => {
    tab.addEventListener('click', () => switchTo(tab.dataset.region));
  });

  let detectedRegion = 'intl';
  try {
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone || '';
    if (tz.includes('Asia/Shanghai') || tz.includes('Asia/Chongqing') ||
        tz.includes('Asia/Harbin') || tz.includes('Asia/Urumqi')) {
      detectedRegion = 'cn';
    }
  } catch (e) {}
  if (detectedRegion === 'intl') {
    const lang = (navigator.language || navigator.userLanguage || '').toLowerCase();
    if (lang.startsWith('zh')) detectedRegion = 'cn';
  }
  const params = new URLSearchParams(window.location.search);
  if (params.get('region') === 'cn') detectedRegion = 'cn';
  if (params.get('region') === 'intl') detectedRegion = 'intl';
  switchTo(detectedRegion);
})();

// ===== 邮箱一键复制（仅反馈页） =====
(function emailCopy() {
  const chip = document.getElementById('email-chip');
  const text = document.getElementById('email-text');
  if (!chip || !text) return;
  chip.addEventListener('click', async () => {
    const email = chip.dataset.email;
    try {
      await navigator.clipboard.writeText(email);
    } catch (e) {
      const ta = document.createElement('textarea');
      ta.value = email;
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      try { document.execCommand('copy'); } catch (_) {}
      document.body.removeChild(ta);
    }
    const original = text.textContent;
    text.textContent = '✓ 已复制到剪贴板';
    chip.classList.add('copied');
    setTimeout(() => {
      text.textContent = original;
      chip.classList.remove('copied');
    }, 1600);
  });
})();