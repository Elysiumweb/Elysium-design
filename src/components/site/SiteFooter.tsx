import { Link } from "@tanstack/react-router";

import { BRAND, SOCIALS } from "@/lib/elysium-data";
import { NAV } from "./SiteHeader";

export function SiteFooter() {
  return (
    <footer className="relative z-10 mt-24 border-t border-border bg-background/80 backdrop-blur-xl">
      <div className="overflow-hidden border-b border-border py-6">
        <div className="flex w-max animate-marquee gap-12 whitespace-nowrap font-display text-4xl font-black uppercase sm:text-6xl">
          {Array.from({ length: 8 }).map((_, i) => (
            <span key={i} className={i % 2 ? "text-gold" : "text-outline"}>
              Not given. Earned.
            </span>
          ))}
        </div>
      </div>
      <div className="mx-auto grid max-w-7xl gap-12 px-5 py-14 sm:px-8 md:grid-cols-[1.4fr_1fr_1fr]">
        <div>
          <img src={BRAND.horizontal} alt="Elysium" className="h-9 w-auto" width={963} height={304} loading="lazy" />
          <p className="mt-5 max-w-sm text-muted-foreground">
            Équipe esport française. Une place réservée aux héros et aux guerriers qui ont prouvé leur valeur.
          </p>
        </div>
        <div>
          <p className="eyebrow mb-4">Explorer</p>
          <ul className="space-y-2">
            {[...NAV, { to: "/soutenir", label: "Soutenir" } as const].map((item) => (
              <li key={item.to}>
                <Link to={item.to} className="text-muted-foreground transition-colors hover:text-primary">
                  {item.label}
                </Link>
              </li>
            ))}
          </ul>
        </div>
        <div>
          <p className="eyebrow mb-4">Suivez-nous</p>
          <ul className="space-y-2">
            {SOCIALS.map((s) => (
              <li key={s.label}>
                <a href={s.href} target="_blank" rel="noreferrer" className="text-muted-foreground transition-colors hover:text-primary">
                  {s.label}
                </a>
              </li>
            ))}
          </ul>
        </div>
      </div>
      <div className="border-t border-border">
        <p className="mx-auto max-w-7xl px-5 py-6 text-xs text-muted-foreground sm:px-8">
          © 2026 Elysium — association loi 1901 (RNA : W772011943). Tous droits réservés.
        </p>
      </div>
    </footer>
  );
}
