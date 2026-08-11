import assert from "node:assert/strict";
import { access, readFile, stat } from "node:fs/promises";
import path from "node:path";
import test from "node:test";
import { fileURLToPath, pathToFileURL } from "node:url";

const here = path.dirname(fileURLToPath(import.meta.url));
const root = path.resolve(here, "..");

async function render() {
  const serverPath = path.join(root, "dist", "server", "index.js");
  await access(serverPath);
  const server = await import(`${pathToFileURL(serverPath).href}?t=${Date.now()}`);
  const response = await server.default.fetch(new Request("http://atlas.test/"));
  return { response, html: await response.text() };
}

test("완성된 Atlas가 한국어 동적 학습 사이트로 렌더링된다", async () => {
  const { response, html } = await render();

  assert.equal(response.status, 200);
  assert.match(response.headers.get("content-type") ?? "", /text\/html/);
  assert.match(html, /lang="ko"/);
  assert.match(html, /NoHome Backend Atlas/);
  assert.match(html, /직접 움직여 보세요/);
  assert.match(html, /초보자를 위한 인터랙티브 백엔드 학습 사이트/);

  for (const sectionId of ["map", "lab", "api", "data", "runtime", "glossary"]) {
    assert.match(html, new RegExp(`id="${sectionId}"`));
  }

  assert.match(html, /draggable="true"/);
  assert.match(html, /type="range"/);
  assert.match(html, /aria-label="API 검색"/);
  assert.match(html, /25(?:<!-- -->)?개/);
  assert.doesNotMatch(html, /codex-preview|react-loading-skeleton/i);
});

test("소스에 이동·조절·반응형·접근성 장치가 포함된다", async () => {
  const [page, css, layout, packageText, og] = await Promise.all([
    readFile(path.join(root, "app", "page.tsx"), "utf8"),
    readFile(path.join(root, "app", "globals.css"), "utf8"),
    readFile(path.join(root, "app", "layout.tsx"), "utf8"),
    readFile(path.join(root, "package.json"), "utf8"),
    stat(path.join(root, "public", "og.png")),
  ]);

  assert.match(page, /^"use client";/);
  assert.match(page, /useState|useEffect/);
  assert.match(page, /onDragStart|onDrop/);
  assert.match(page, /type="range"/);
  assert.equal((page.match(/\{ method: "(?:GET|POST|PUT|DELETE)"/g) ?? []).length, 25);
  assert.match(page, /\/api\/interest-regions\/\{interestRegionId\}/);
  assert.match(page, /\/api\/notices\/\{noticeId\}/);

  assert.match(css, /@media \(max-width:/);
  assert.match(css, /@media \(prefers-reduced-motion: reduce\)/);
  assert.match(css, /word-break: keep-all/);
  assert.match(layout, /headers\(\)/);
  assert.match(layout, /\/og\.png/);

  const packageJson = JSON.parse(packageText);
  assert.equal(packageJson.name, "nohome-backend-atlas");
  assert.equal(packageJson.dependencies?.["react-loading-skeleton"], undefined);
  assert.ok(og.size > 100_000, "OG 이미지는 실제 생성된 이미지여야 합니다.");

  await assert.rejects(access(path.join(root, "app", "_sites-preview", "SkeletonPreview.tsx")));
  await assert.rejects(access(path.join(root, "app", "_sites-preview", "preview.css")));
});
