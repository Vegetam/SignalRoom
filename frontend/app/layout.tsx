import type { Metadata } from 'next';
import '@livekit/components-styles';
import './globals.css';
export const metadata: Metadata = { title: 'SignalRoom | WebRTC Meetings', description: 'Self-hosted real-time conferencing with transport diagnostics' };
export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
 return <html lang="en"><body>{children}</body></html>;
}
