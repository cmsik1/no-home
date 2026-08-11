import type { Metadata } from "next";
import { headers } from "next/headers";
import "./globals.css";

export async function generateMetadata(): Promise<Metadata> {
  const incoming = await headers();
  const host = incoming.get("x-forwarded-host") ?? incoming.get("host") ?? "localhost:3000";
  const protocol = incoming.get("x-forwarded-proto") ?? (host.includes("localhost") ? "http" : "https");
  const origin = `${protocol}://${host}`;
  return {
    title: "NoHome Backend Atlas | 직접 움직이며 배우는 백엔드 지도",
    description: "블록을 옮기고 파이프라인을 실행하며 NoHome Spring Boot 백엔드를 쉽게 배우는 인터랙티브 사이트",
    openGraph: {
      title: "NoHome Backend Atlas",
      description: "직접 움직이며 배우는 백엔드 지도",
      images: [{ url: `${origin}/og.png`, width: 1200, height: 630, alt: "NoHome Backend Atlas 인터랙티브 학습 지도" }],
    },
    twitter: { card: "summary_large_image", title: "NoHome Backend Atlas", description: "직접 움직이며 배우는 백엔드 지도", images: [`${origin}/og.png`] },
  };
}

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="ko"><body>{children}</body></html>;
}
