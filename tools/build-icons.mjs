#!/usr/bin/env node
/**
 * Render the complete supplied logo (symbol + wordmark + tagline) into adaptive
 * icon-safe PNGs. This intentionally fits the SVG's full content into Android's
 * 66dp adaptive-icon safe circle; no part of AFYALINK or its tagline is cropped.
 *
 * Rebuild: npm ci --prefix tools && node tools/build-icons.mjs
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { initWasm, Resvg } from '@resvg/resvg-wasm';
import opentype from 'opentype.js';

const here = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(here, '..');
const sourcePath = path.join(root, 'brand', 'afyalink-logo.svg');
const out = path.join(root, 'android', 'app', 'src', 'main', 'res');
const wasmPath = path.join(here, 'node_modules', '@resvg', 'resvg-wasm', 'index_bg.wasm');
await initWasm(fs.readFileSync(wasmPath));

let source = fs.readFileSync(sourcePath, 'utf8')
  .replace(/<!DOCTYPE[\s\S]*?>/i, '')
  .replace(/<\?xml[^>]*\?>/i, '');
// The source stores its wordmark/tagline as live Impact/Poppins text, which
// Android's VectorDrawable and resvg do not support. Convert both to outline
// paths at the SVG's original baselines with license-clean DejaVu font files.
function loadFont(file) {
  const data = fs.readFileSync(path.join(here, 'fonts', file));
  return opentype.parse(data.buffer.slice(data.byteOffset, data.byteOffset + data.byteLength));
}
const wordFont = loadFont('DejaVuSans-Bold.ttf');
const taglineFont = loadFont('DejaVuSans.ttf');
const wordPath = wordFont.getPath('AFYALINK', 291, 1564, 294.566).toPathData(3);
const taglinePath = taglineFont.getPath('HEALTH, CLOSER TO YOU', 291, 1564, 294.566).toPathData(3);
// Condense the bold wordmark to echo the Impact-like source proportions.
const wordMarkup = `<g transform="matrix(0.78,0,0,1,64,0)"><path style="fill:rgb(9,93,126);" d="${wordPath}" /></g>`;
const taglineMarkup = `<path style="fill:rgb(20,23,23);" d="${taglinePath}" />`;
source = source.replace(/<text[^>]*>AF[\s\S]*?<\/text>/, wordMarkup);
source = source.replace(/<text[^>]*>HEALTH, CLOSER TO YOU<\/text>/, taglineMarkup);

// Include the logo mark and both transformed text baselines in one square
// viewBox. The source wordmark runs roughly x=470..1740 and the tagline
// x=460..1600 in the original 2000-unit canvas; the symbol spans x=734..1266.
// A conservative enclosing radius keeps all text within the launcher safe mask.
const bbox = { x: 455, y: 398, width: 1300, height: 1260 };
const cx = bbox.x + bbox.width / 2;
const cy = bbox.y + bbox.height / 2;
const radius = Math.hypot(bbox.width / 2, bbox.height / 2);
const halfSide = radius * (54 / 32.2);
const viewBox = `${cx - halfSide} ${cy - halfSide} ${halfSide * 2} ${halfSide * 2}`;
const reViewBox = source.replace(/viewBox="[^"]+"/, `viewBox="${viewBox}"`);

function render(svg, size) {
  const image = new Resvg(svg, {
    fitTo: { mode: 'width', value: size },
    textRendering: 2,
    shapeRendering: 2,
  }).render();
  const png = Buffer.from(image.asPng());
  image.free();
  return png;
}

const full = render(reViewBox, 512);
const monoSvg = reViewBox
  .replace(/fill:rgb\([^)]*\)/g, 'fill:rgb(255,255,255)')
  .replace(/fill="#[0-9a-fA-F]{3,8}"/g, 'fill="#ffffff"');
const mono = render(monoSvg, 512);
const nodpi = path.join(out, 'drawable-nodpi');
fs.mkdirSync(nodpi, { recursive: true });
fs.writeFileSync(path.join(nodpi, 'ic_launcher_full_logo.png'), full);
fs.writeFileSync(path.join(nodpi, 'ic_launcher_full_logo_monochrome.png'), mono);

const densities = { 'mdpi': 48, 'hdpi': 72, 'xhdpi': 96, 'xxhdpi': 144, 'xxxhdpi': 192 };
for (const [density, pixels] of Object.entries(densities)) {
  const dir = path.join(out, `mipmap-${density}`);
  fs.mkdirSync(dir, { recursive: true });
  fs.writeFileSync(path.join(dir, 'ic_launcher.png'), render(reViewBox, pixels));
  fs.writeFileSync(path.join(dir, 'ic_launcher_round.png'), render(reViewBox, pixels));
}
console.log(`Logo content bounds ${bbox.x.toFixed(1)},${bbox.y.toFixed(1)} ${bbox.width.toFixed(1)}×${bbox.height.toFixed(1)}; safe-radius scale ${ (32.2 / radius).toFixed(4)}.`);
console.log('Generated color + monochrome full-logo adaptive foregrounds and legacy launcher icons.');
