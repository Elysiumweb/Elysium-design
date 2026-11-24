import { createFileRoute, Link } from "@tanstack/react-router";

import { Container, PageHero } from "@/components/site/Headings";
import { Reveal } from "@/components/site/Reveal";
import { TiltCard } from "@/components/site/TiltCard";
import { BRAND, PLAYERS, type Game } from "@/lib/elysium-data";
import { cn } from "@/lib/utils";

type Filter = "Tous" | Game;
const FILTERS: Filter[] = ["Tous", "EVA", "Rocket League"];

export const Route = createFileRoute("/equipe")({
  validateSearch: (search: Record<string, unknown>): { jeu?: Game } => {
    const j = search["jeu"];
    return j === "EVA" || j === "Rocket League" ? { jeu: j } : {};
  },
  head: () => ({
    meta: [
      { title: "L'équipe — Elysium Esport" },
      { name: "description", content: "Les guerriers qui portent les couleurs d'Elysium sur EVA et Rocket League." },
      { property: "og:title", content: "L'équipe — Elysium Esport" },
      { property: "og:description", content: "Découvre les joueurs EVA et Rocket League d'Elysium." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: TeamPage,
});

function TeamPage() {
  const { jeu } = Route.useSearch();
  const active: Filter = jeu ?? "Tous";
  const players = PLAYERS.filter((p) => active === "Tous" || p.game === active);

  return (
    <>
      <PageHero
        eyebrow="EVA & Rocket League"
        title="L'équipe"
        subtitle="Les guerriers qui portent les couleurs d'Elysium."
      >
        <div className="flex flex-wrap gap-2" role="tablist" aria-label="Filtrer par jeu">
          {FILTERS.map((f) => {
            const count = f === "Tous" ? PLAYERS.length : PLAYERS.filter((p) => p.game === f).length;
            return (
              <Link
                key={f}
                to="/equipe"
                search={f === "Tous" ? {} : { jeu: f }}
                role="tab"
                aria-selected={active === f}
                className={cn(
                  "clip-blade-sm border px-5 py-3 font-label text-xs font-semibold uppercase tracking-[0.22em] backdrop-blur transition-colors",
                  active === f
                    ? "border-primary bg-primary text-primary-foreground"
                    : "border-border bg-background/50 text-muted-foreground hover:border-primary hover:text-primary",
                )}
              >
                {f} ({count})
              </Link>
            );
          })}
        </div>
      </PageHero>

      <section className="relative pb-16">
        <Container>
          {players.length > 0 ? (
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
              {players.map((p, i) => (
                <Reveal key={p.name} delay={i * 90}>
                  <TiltCard max={12} className="panel clip-blade group h-full overflow-hidden">
                    <div className="relative aspect-[3/4] overflow-hidden">
                      <img
                        src={p.photo}
                        alt={p.name}
                        className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-110"
                        loading="lazy"
                      />
                      <div className="absolute inset-0 bg-gradient-to-t from-background via-background/30 to-transparent" />
                      <span className="absolute right-4 top-4 bg-background/70 px-3 py-1 font-label text-[0.65rem] font-bold uppercase tracking-[0.22em] text-primary backdrop-blur clip-blade-sm">
                        {p.game}
                      </span>
                      <div className="tilt-pop-lg absolute inset-x-0 bottom-0 p-6">
                        <p className="eyebrow mb-1">{p.role}</p>
                        <h2 className="font-display text-3xl font-black uppercase">{p.name}</h2>
                      </div>
                    </div>
                    {p.bio && <p className="tilt-pop p-6 pt-4 text-muted-foreground">{p.bio}</p>}
                  </TiltCard>
                </Reveal>
              ))}
            </div>
          ) : (
            <Reveal>
              <div className="panel clip-blade flex flex-col items-center gap-5 p-12 text-center">
                <img src={BRAND.icon} alt="" className="h-16 w-16 animate-float-slow" />
                <p className="font-display text-2xl font-bold uppercase">Roster en cours de composition</p>
                <p className="max-w-md text-muted-foreground">
                  Le pôle Rocket League (Ascend & Mechanical) se construit. Tu penses avoir le niveau ?
                </p>
                <Link to="/recrutement" className="font-label text-xs font-semibold uppercase tracking-[0.22em] text-primary">
                  Postuler →
                </Link>
              </div>
            </Reveal>
          )}
        </Container>
      </section>
    </>
  );
}
