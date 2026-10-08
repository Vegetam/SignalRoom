from pathlib import Path
import json,re
p=Path(__file__).resolve().parents[1]
package=json.loads((p/'frontend/package.json').read_text())
client=tuple(map(int,package['dependencies']['livekit-client'].split('.')[:3]))
assert client >= (2,20,1), 'LiveKit peer dependency mismatch'
api=(p/'backend/src/main/java/com/example/meet/CollaborationController.java').read_text()
for table in ('sr_users','sr_sessions','sr_teams','sr_members','sr_channels','sr_messages','sr_events','sr_files'):
 assert 'CREATE TABLE IF NOT EXISTS '+table in api,table
for endpoint in ('/auth/register','/auth/login','/teams','/channels/{channel}/messages','/events','/teams/{team}/files','/files/{id}'):
 assert endpoint in api,endpoint
ui=(p/'frontend/app/dashboard/page.tsx').read_text()
for feat in ('Start a meeting','Shared files','Schedule an event','Manage teams','Message this channel','Create account'):
 assert feat in ui,feat
print('PASS: LiveKit dependency floor, 8 database tables, 7 API routes, 6 dashboard flows')
