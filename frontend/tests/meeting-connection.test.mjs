import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
const source=readFileSync(new URL('../app/room/[roomId]/page.tsx',import.meta.url),'utf8');
test('meeting join does not retrigger when credentials are set',()=>{
 assert.match(source,/if\(!key\|\|credentials\)return;/);
 assert.match(source,/if\(!key\|\|joiningRef\.current\)return;/);
 assert.match(source,/joiningRef\.current=true;/);
});
