import { createFileRoute, Link } from "@tanstack/react-router";
import { useState } from "react";

import { Container, PageHero } from "@/components/site/Headings";
import { Reveal } from "@/components/site/Reveal";
import { TiltCard } from "@/components/site/TiltCard";
import { BRAND, RESULTS, SEASON_STATS, formatMatchDate, isWin } from "@/lib/elysium-data";
import { cn } from "@/lib/utils";

export const Route = createFileRoute("/resultats")({
  head: () => ({
    meta: [
      { title: "Résultats des matchs — Elysium Esport" },
      { name: "description", content: "Tous les matchs officiels d'Elysium sur EVA et Rocket League : scores, compétitions et MVP." },
      { property: "og:title", content: "Résultats des matchs — Elysium Esport" },
      { property: "og:description", content: "Scores, compétitions et MVP des matchs officiels d'Elysium." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: ResultsPage,
});

type Outcome = "Tous" | "Victoires" | "Défaites";

function ResultsPage() {
  const [outcome, setOutcome] = useState<Outcome>("Tous");
  const list = RESULTS.filter((r) => (outcome === "Tous" ? true : outcome === "Victoires" ? isWin(r) : !isWin(r)));

  return (
    <>
      <PageHero eyebrow="Matchs officiels" title="Résultats" subtitle="Chaque victoire se mérite. Voici les preuves.">
        <div className="grid max-w-lg grid-cols-2 gap-4">
          <div className="panel clip-blade-sm p-5">
            <p className="font-display text-4xl font-black text-gold">{SEASON_STATS.matches}</p>
            <p className="font-label text-xs uppercase tracking-[0.22em] text-muted-foreground">matchs joués</p>
          </div>
          <div className="panel clip-blade-sm p-5">
            <p className="font-display text-4xl font-black text-gold">{SEASON_STATS.winRate}%</p>
            <p className="font-label text-xs uppercase tracking-[0.22em] text-muted-foreground">de victoires</p>
          </div>
        </div>
      </PageHero>

      <section className="relative pb-16">
        <Container>
          <div className="mb-8 flex flex-wrap items-center justify-between gap-4">
            <div className="flex flex-wrap gap-2">
              {(["Tous", "Victoires", "Défaites"] as Outcome[]).map((o) => (
                <button
                  key={o}
                  type="button"
                  onClick={() => setOutcome(o)}
                  aria-pressed={outcome === o}
                  className={cn(
                    "clip-blade-sm border px-5 py-3 font-label text-xs font-semibold uppercase tracking-[0.22em] backdrop-blur transition-colors",
                    outcome === o
                      ? "border-primary bg-primary text-primary-foreground"
                      : "border-border bg-background/50 text-muted-foreground hover:border-primary hover:text-primary",
                  )}
                >
                  {o}
                </button>
              ))}
            </div>
            <Link to="/calendrier" className="font-label text-xs font-semibold uppercase tracking-[0.22em] text-primary">
              S'abonner aux matchs →
            </Link>
          </div>

          <div className="space-y-4">
            {list.map((r, i) => {
              const win = isWin(r);
              return (
                <Reveal key={`${r.date}-${r.opponent}`} delay={Math.min(i, 6) * 60}>
                  <TiltCard max={3} className="panel clip-blade overflow-hidden">
                    <span className={cn("absolute inset-y-0 left-0 w-1", win ? "bg-victory" : "bg-defeat")} />
                    <div className="grid items-center gap-4 p-5 sm:grid-cols-[1fr_auto_1fr] sm:p-6">
                      <div className="flex items-center gap-4">
                        <img src={BRAND.icon} alt="" className="h-10 w-10 object-contain" loading="lazy" />
                        <div>
                          <p className="font-display text-base font-bold uppercase sm:text-lg">{r.team}</p>
                          <p className="font-label text-[0.65rem] uppercase tracking-[0.2em] text-muted-foreground">
                            {r.game}
                            {r.mvp ? ` · MVP ${r.mvp}` : ""}
                          </p>
                        </div>
                      </div>
                      <div className="tilt-pop text-center">
                        <p
                          className={cn(
                            "font-label text-[0.65rem] font-bold uppercase tracking-[0.3em]",
                            win ? "text-victory" : "text-defeat",
                          )}
                        >
                          {win ? "Victoire" : "Défaite"}
                        </p>
                        <p className="font-display text-4xl font-black">
                          <span className={win ? "text-gold" : "text-foreground"}>{r.us}</span>
                          <span className="mx-2 text-muted-foreground">—</span>
                          <span className={win ? "text-muted-foreground" : "text-defeat"}>{r.them}</span>
                        </p>
                      </div>
                      <div className="sm:text-right">
                        <p className="font-display text-base font-bold uppercase sm:text-lg">{r.opponent}</p>
                        <p className="text-sm text-muted-foreground">
                          {formatMatchDate(r.date)} · {r.competition}
                        </p>
                      </div>
                    </div>
                  </TiltCard>
                </Reveal>
              );
            })}
          </div>
          <p className="mt-6 text-sm text-muted-foreground">
            {list.length} derniers matchs affichés sur {SEASON_STATS.matches}.
          </p>
        </Container>
      </section>
    </>
  );
}
