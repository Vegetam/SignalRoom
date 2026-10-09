"use client";
import {FormEvent,Suspense,useEffect,useState} from 'react';
import {useRouter,useSearchParams} from 'next/navigation';
const API=process.env.NEXT_PUBLIC_API_URL||'http://localhost:8080';
const VALID_ROOM=/^[A-Za-z0-9_-]{3,64}$/;
type History={room:string;status:string;created:string};
function MeetingLobby(){
 const router=useRouter();const search=useSearchParams();
 const [room,setRoom]=useState(''),[name,setName]=useState(''),[action,setAction]=useState<'create'|'join'>('create');
 const [busy,setBusy]=useState(false),[error,setError]=useState(''),[history,setHistory]=useState<History[]>([]),[notice,setNotice]=useState('');
 useEffect(()=>{const invited=search.get('room');if(invited){setAction('join');setRoom(invited);}},[search]);
 useEffect(()=>{const entries=Object.keys(sessionStorage).filter(k=>k.startsWith('signalroom:')&&k.endsWith(':role')&&sessionStorage.getItem(k)==='HOST');Promise.all(entries.map(async k=>{const r=k.slice('signalroom:'.length,-':role'.length);const host=sessionStorage.getItem(`signalroom:${r}:key`);if(!host)return [];try{const res=await fetch(API+'/api/meetings/history',{headers:{'X-Host-Key':host}});return res.ok?await res.json():[]}catch{return []}})).then(rows=>setHistory(rows.flat().filter((v,i,a)=>a.findIndex(x=>x.room===v.room)===i)));},[]);
 async function submit(e:FormEvent){e.preventDefault();setBusy(true);setError('');try{
  if(action==='join'&&!VALID_ROOM.test(room))throw Error('Enter a valid meeting ID or use an invitation link.');
  const path=action==='create'?'/api/meetings':'/api/admissions';
  const payload=action==='create'?{displayName:name.trim()}:{room:room.trim(),displayName:name.trim()};
  const response=await fetch(API+path,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload)});
  const data=await response.json();if(!response.ok)throw Error(data.error||data.detail||`Request failed: ${response.status}`);
  const meeting=action==='create'?data.room:room.trim();
  sessionStorage.setItem(`signalroom:${meeting}:key`,action==='create'?data.hostKey:data.guestKey);
  sessionStorage.setItem(`signalroom:${meeting}:role`,action==='create'?'HOST':'GUEST');
  router.push(`/room/${encodeURIComponent(meeting)}`);
 }catch(e){setError(e instanceof Error?e.message:String(e))}finally{setBusy(false)}}
 const copyInvite=async(id:string)=>{const link=`${location.origin}/meet?room=${encodeURIComponent(id)}`;try{await navigator.clipboard.writeText(link);setNotice('Invitation link copied');}catch{setNotice(link)}};
 return <main className="hero"><div style={{display:'flex',justifyContent:'flex-end'}}><a href="/dashboard" className="secondary">← Back to workspace</a></div><header className="brand">◈ Signal<span>Room</span></header><section className="landing"><div><span className="pill">60-PERSON SFU MEETINGS</span><h1>Meet in one click.<br/>Share a secure invitation.</h1><p className="muted">Just enter your name to start a meeting. SignalRoom generates a unique 9-digit meeting ID. Share the invitation link and admit guests from the waiting room.</p><div className="features"><span>Automatic meeting ID</span><span>Shareable link</span><span>Host approval</span><span>LiveKit SFU</span></div></div><form className="panel" onSubmit={submit}><h2>{action==='create'?'Start a new meeting':'Join a meeting'}</h2><div className="tabs"><button type="button" className="secondary" aria-pressed={action==='create'} onClick={()=>{setAction('create');setError('')}}>New meeting</button><button type="button" className="secondary" aria-pressed={action==='join'} onClick={()=>{setAction('join');setError('')}}>Join with an ID</button></div><label className="field">Display name<input required maxLength={60} value={name} onChange={e=>setName(e.target.value)}/></label>{action==='join'?<label className="field">Meeting ID<input required pattern="[A-Za-z0-9_-]{3,64}" autoComplete="off" placeholder="e.g. 482719365" value={room} onChange={e=>setRoom(e.target.value.replace(/\s/g,''))}/></label>:<p className="muted">Your meeting ID will be generated automatically. No code to enter.</p>}{error&&<p className="error" role="alert">{error}</p>}<button className="primary" disabled={busy}>{busy?'Please wait…':action==='create'?'Start meeting':'Request to join'}</button><p className="muted" style={{fontSize:12}}>Development mode: host keys are stored in this browser session only. Guest links never contain host credentials.</p><section className="history"><h3>Meetings hosted in this browser</h3>{notice&&<p className="muted" role="status">{notice}</p>}{history.length===0?<p className="muted">No previously hosted meetings on this device.</p>:history.map(item=><div key={item.room} className="waiting-entry"><span>{item.room} · {item.status} · {String(item.created).slice(0,16)}</span><div style={{display:'flex',gap:6}}><button type="button" className="secondary" onClick={()=>void copyInvite(item.room)}>Copy invite</button><button type="button" className="secondary" onClick={()=>router.push(`/room/${encodeURIComponent(item.room)}`)}>Open</button></div></div>)}<p className="muted" style={{fontSize:11}}>Cross-device account-linked history requires a later identity integration.</p></section></form></section></main>
}
export default function Home(){return <Suspense fallback={<main className="hero">Loading meeting lobby…</main>}><MeetingLobby/></Suspense>;}
