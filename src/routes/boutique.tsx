import { createFileRoute } from "@tanstack/react-router";
import { ArrowUpRight, Check } from "lucide-react";
import { useState } from "react";

import { Container, PageHero } from "@/components/site/Headings";
import { Reveal } from "@/components/site/Reveal";
import { TiltCard } from "@/components/site/TiltCard";
import { Button } from "@/components/ui/button";
import { PRODUCTS } from "@/lib/elysium-data";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/boutique")({
  head: () => ({
    meta: [
      { title: "Boutique officielle — Elysium Esport" },
      { name: "description", content: "Maillot officiel Elysium 2026 (49,90 €) et manchettes. Fabriqués en France." },
      { property: "og:title", content: "Boutique officielle — Elysium Esport" },
      { property: "og:description", content: "Portez nos couleurs : maillot officiel et manchettes Elysium 2026." },
      { property: "og:type", content: "website" },
      { property: "og:image", content: PRODUCTS.jersey.image },
      { name: "twitter:card", content: "summary_large_image" },
      { name: "twitter:image", content: PRODUCTS.jersey.image },
    ],
  }),
  component: ShopPage,
});

function ShopPage() {
  const { jersey, sleeves } = PRODUCTS;
  const [size, setSize] = useState("M");

  return (
    <>
      <PageHero eyebrow="Official merch • 2026" title="Boutique" subtitle="Portez nos couleurs. Le maillot officiel et les manchettes Elysium 2026 sont disponibles." />

      <section className="relative pb-20">
        <Container className="grid items-center gap-12 lg:grid-cols-2">
          <Reveal>
            <TiltCard max={10} className="clip-blade overflow-hidden shadow-gold">
              <img src={jersey.image} alt="Maillot officiel Elysium Esport 2026" className="aspect-square w-full object-cover" />
            </TiltCard>
          </Reveal>
          <Reveal delay={120}>
            <p className="eyebrow mb-3">Maillot officiel · Elysium 2026</p>
            <h2 className="font-display text-4xl font-black uppercase sm:text-5xl">{jersey.name}</h2>
            <p className="mt-4 font-display text-4xl font-black text-gold">
              {jersey.price} <span className="font-label text-sm text-muted-foreground">TTC</span>
            </p>
            <p className="mt-5 text-lg text-muted-foreground">{jersey.text}</p>
            <ul className="mt-5 space-y-2">
              {jersey.features.map((f) => (
                <li key={f} className="flex items-center gap-3">
                  <Check className="size-4 text-primary" /> {f}
                </li>
              ))}
            </ul>
            <p className="eyebrow mb-3 mt-8">Tailles disponibles</p>
            <div className="flex flex-wrap gap-2">
              {jersey.sizes.map((s) => (
                <button
                  key={s}
                  type="button"
                  onClick={() => setSize(s)}
                  aria-pressed={size === s}
                  className={cn(
                    "h-11 min-w-12 border px-3 font-label text-sm font-semibold clip-blade-sm transition-colors",
                    size === s ? "border-primary bg-primary text-primary-foreground" : "border-border bg-background/50 hover:border-primary",
                  )}
                >
                  {s}
                </button>
              ))}
            </div>
            <Button asChild variant="gold" size="xl" className="mt-8 w-full sm:w-auto">
              <a href={jersey.href} target="_blank" rel="noreferrer">
                Acheter le maillot — {jersey.price} <ArrowUpRight />
              </a>
            </Button>
            <p className="mt-3 text-sm text-muted-foreground">
              Taille choisie sur eliminate.fr. Commande et paiement sécurisés sur eliminate.fr.
            </p>
          </Reveal>
        </Container>
      </section>

      <section className="relative py-20">
        <Container className="grid items-center gap-12 lg:grid-cols-2">
          <Reveal className="lg:order-2">
            <TiltCard max={10} className="clip-blade overflow-hidden shadow-gold">
              <img src={sleeves.image} alt="Manchettes officielles Elysium Esport 2026" className="aspect-square w-full object-cover" loading="lazy" />
            </TiltCard>
          </Reveal>
          <Reveal delay={120} className="lg:order-1">
            <p className="eyebrow mb-3">Accessoire officiel · Elysium 2026</p>
            <h2 className="font-display text-4xl font-black uppercase sm:text-5xl">{sleeves.name}</h2>
            <p className="mt-5 text-lg text-muted-foreground">{sleeves.text}</p>
            <div className="mt-6 grid max-w-sm grid-cols-2 gap-3">
              {sleeves.prices.map((p) => (
                <div key={p.label} className="panel clip-blade-sm p-5">
                  <p className="font-label text-xs uppercase tracking-[0.22em] text-muted-foreground">{p.label}</p>
                  <p className="mt-1 font-display text-2xl font-black text-gold">{p.price}</p>
                </div>
              ))}
            </div>
            <Button asChild variant="gold" size="xl" className="mt-8 w-full sm:w-auto">
              <a href={sleeves.href} target="_blank" rel="noreferrer">
                Acheter les manchettes <ArrowUpRight />
              </a>
            </Button>
          </Reveal>
        </Container>
      </section>
    </>
  );
}
