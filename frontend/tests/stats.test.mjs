import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
test('stats module exports the aggregator',()=>{ const code=readFileSync(new URL('../lib/stats.ts', import.meta.url),'utf8');assert.match(code,/summariseReports/);assert.match(code,/inbound-rtp/);});
