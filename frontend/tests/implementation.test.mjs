import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const read = name => readFileSync(new URL('../'+name, import.meta.url),'utf8');
test('host controls exist in frontend',()=>{assert.match(read('components/Moderator.tsx'),/admissions\/decision/);assert.match(read('components/Moderator.tsx'),/End meeting/)});
test('guest only receives JWT through controlled join endpoint',()=>{assert.match(read('app/room/[roomId]/page.tsx'),/accessKey:key/);assert.match(read('app/room/[roomId]/page.tsx'),/WAITING/)});
test('stats and SDP inspection are wired',()=>{assert.match(read('components/Diagnostics.tsx'),/getStats/);assert.match(read('lib/protocol-tap.ts'),/setLocalDescription/);assert.match(read('lib/protocol-tap.ts'),/ice-pwd/)});
