export type MediaStats={iceState?:string;pair?:string;rttMs?:number;outKbps?:number;packetsLost?:number;jitterMs?:number;codec?:string};
export function summariseReports(report:RTCStatsReport):MediaStats {
 const result:MediaStats={}; let sent=0; let selectedId:string|undefined;
 report.forEach((raw)=>{
  const entry = raw as any;
  if(entry.type==='transport'){selectedId=entry.selectedCandidatePairId;result.iceState=entry.dtlsState;}
  if(entry.type==='outbound-rtp' && !entry.isRemote){sent+=entry.bytesSent||0;}
  if(entry.type==='inbound-rtp' && !entry.isRemote){result.packetsLost=(result.packetsLost||0)+(entry.packetsLost||0);if(entry.jitter!=null)result.jitterMs=Math.round(entry.jitter*1000);}
  if(entry.type==='codec' && !result.codec){result.codec=entry.mimeType;}
 });
 if(selectedId){const pair=report.get(selectedId);if(pair){result.pair=pair.localCandidateId+' / '+pair.remoteCandidateId;if(pair.currentRoundTripTime!=null)result.rttMs=Math.round(pair.currentRoundTripTime*1000);}}
 // Bandwidth calculation needs two samples; emitted in the component's transport diagnostics when supported.
 return result;
}
