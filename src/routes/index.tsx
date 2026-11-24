import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowRight, ArrowUpRight, Radio, Swords, Trophy, Users } from "lucide-react";

import { Container, RiseTitle, SectionHeading } from "@/components/site/Headings";
import { Reveal } from "@/components/site/Reveal";
import { TiltCard } from "@/components/site/TiltCard";
import { Button } from "@/components/ui/button";
import {
  BRAND,
  DISCORD_URL,
  PLAYERS,
  PRODUCTS,
  RESULTS,
  SEASON_STATS,
  SOCIALS,
  TWITCH_URL,
  YOUTUBE_URL,
  formatMatchDate,
} from "@/lib/elysium-data";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "Elysium Esport — Not given. Earned." },
      {
        name: "description",
        content: "Équipe esport française sur EVA et Rocket League. Résultats, effectif, boutique officielle et communauté Elysium.",
      },
      { property: "og:title", content: "Elysium Esport — Not given. Earned." },
      {
        property: "og:description",
        content: "Équipe esport française sur EVA et Rocket League. Une place réservée aux héros.",
      },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: Home,
});

const GAMES = [
  {
    code: "EVA",
    name: "Team EVA",
    sub: "Esports Virtual Arenas",
    text: "Esport virtuel en arène. Notre roster EVA représente Elysium en compétition.",
    game: "EVA",
  },
  {
    code: "RL",
    name: "Team Rocket League",
    sub: "Football de voitures",
    text: "Nouveau pôle RL, même exigence, même écusson. Rosters Ascend et Mechanical.",
    game: "Rocket League",
    tag: "Nouveau",
  },
] as const;

function Home() {
  const last = RESULTS[0]!;
  return (
    <>
      {/* HERO */}
      <section className="relative flex min-h-svh items-end pb-14 pt-28 sm:items-center sm:pb-0">
        <Container>
          <div className="max-w-2xl">
            <p className="eyebrow mb-6">Esport team — Est. 2026</p>
            <h1 className="whitespace-nowrap font-display text-[clamp(3.2rem,14vw,7.5rem)] font-black uppercase leading-[0.85]">
              <RiseTitle text="ELYSIUM" className="text-gold" />
            </h1>
            <p className="mt-5 font-display text-lg font-semibold uppercase tracking-[0.45em] text-primary sm:text-2xl">
              Not given. Earned.
            </p>
            <p className="mt-6 max-w-md text-lg text-muted-foreground">
              Équipe esport française. Une place réservée aux héros et aux guerriers qui ont prouvé leur valeur.
            </p>
            <div className="mt-9 flex flex-col gap-3 sm:flex-row">
              <Button asChild variant="gold" size="xl">
                <Link to="/recrutement">
                  Nous rejoindre <ArrowRight />
                </Link>
              </Button>
              <Button asChild variant="frame" size="xl">
                <Link to="/resultats">Voir les résultats</Link>
              </Button>
            </div>
          </div>
        </Container>
        <div className="pointer-events-none absolute bottom-6 left-1/2 hidden -translate-x-1/2 flex-col items-center gap-2 sm:flex">
          <span className="eyebrow text-[0.6rem]">Défiler</span>
          <span className="h-10 w-px animate-pulse bg-primary" />
        </div>
      </section>

      {/* À LA UNE */}
      <section className="relative py-10">
        <Container>
          <Reveal>
            <Link to="/resultats" className="group block">
              <TiltCard max={4} className="panel clip-blade overflow-hidden p-6 sm:p-8">
                <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:justify-between">
                  <div className="tilt-pop">
                    <p className="eyebrow mb-3 flex items-center gap-2">
                      <Radio className="size-4" /> À la une · Dernier résultat
                    </p>
                    <p className="font-display text-xl font-bold uppercase sm:text-3xl">
                      {last.team} <span className="text-gold">{last.us}—{last.them}</span> {last.opponent}
                    </p>
                    <p className="mt-2 text-sm text-muted-foreground">
                      {formatMatchDate(last.date)} · {last.competition} · {SEASON_STATS.winRate} % de victoires
                    </p>
                  </div>
                  <span className="inline-flex items-center gap-2 font-label text-xs font-semibold uppercase tracking-[0.22em] text-primary">
                    Tous les matchs <ArrowUpRight className="size-4 transition-transform group-hover:-translate-y-1 group-hover:translate-x-1" />
                  </span>
                </div>
              </TiltCard>
            </Link>
          </Reveal>
        </Container>
      </section>

      {/* L'ELYSIUM */}
      <section className="relative py-24 sm:py-32">
        <Container className="grid items-center gap-14 lg:grid-cols-2">
          <Reveal>
            <TiltCard max={8} className="panel clip-blade mx-auto aspect-square w-full max-w-md p-10">
              <img
                src={BRAND.vertical}
                alt="Logo vertical Elysium"
                className="tilt-pop-lg h-full w-full animate-float-slow object-contain"
                loading="lazy"
              />
            </TiltCard>
          </Reveal>
          <Reveal delay={120}>
            <p className="eyebrow mb-4">Le mythe</p>
            <h2 className="font-display text-4xl font-black uppercase sm:text-6xl">
              L'<span className="text-gold">Elysium</span>
            </h2>
            <p className="mt-6 text-lg leading-relaxed text-muted-foreground sm:text-xl">
              Dans la mythologie grecque, l'Elysium est la demeure des hommes vertueux après la mort. Un paradis réservé aux
              héros et aux guerriers qui ont prouvé leur valeur.
            </p>
            <p className="mt-6 font-display text-xl font-bold uppercase text-foreground sm:text-2xl">
              Rien n'est donné. <span className="text-gold">Tout se mérite.</span>
            </p>
            <p className="mt-2 text-muted-foreground">C'est le standard pour lequel nous nous battons.</p>
          </Reveal>
        </Container>
      </section>

      {/* NOS JEUX */}
      <section className="relative py-20">
        <Container>
          <Reveal>
            <SectionHeading eyebrow="Deux pôles, même ADN" title={<>Nos <span className="text-gold">jeux</span></>} />
          </Reveal>
          <div className="grid gap-6 md:grid-cols-2">
            {GAMES.map((g, i) => (
              <Reveal key={g.code} delay={i * 120}>
                <Link to="/equipe" search={{ jeu: g.game }} className="block">
                  <TiltCard className="panel clip-blade group h-full min-h-80 overflow-hidden p-8">
                    <span className="pointer-events-none absolute -right-4 -top-10 font-display text-[10rem] font-black leading-none text-outline opacity-60 transition-transform duration-700 group-hover:scale-110">
                      {g.code}
                    </span>
                    <div className="tilt-pop relative flex h-full flex-col justify-end">
                      {"tag" in g && (
                        <span className="mb-4 w-fit bg-gold px-3 py-1 font-label text-[0.65rem] font-bold uppercase tracking-[0.25em] text-primary-foreground clip-blade-sm">
                          {g.tag}
                        </span>
                      )}
                      <p className="eyebrow mb-2">{g.sub}</p>
                      <h3 className="font-display text-3xl font-bold uppercase">{g.name}</h3>
                      <p className="mt-3 max-w-sm text-muted-foreground">{g.text}</p>
                      <span className="mt-6 inline-flex items-center gap-2 font-label text-xs font-semibold uppercase tracking-[0.22em] text-primary">
                        Découvrir l'équipe <ArrowRight className="size-4 transition-transform group-hover:translate-x-1" />
                      </span>
                    </div>
                  </TiltCard>
                </Link>
              </Reveal>
            ))}
          </div>
        </Container>
      </section>

      {/* LES PREUVES */}
      <section className="relative py-20">
        <Container>
          <Reveal>
            <SectionHeading eyebrow="Le palmarès parle" title={<>Les <span className="text-gold">preuves</span></>} />
          </Reveal>
          <div className="grid gap-5 sm:grid-cols-3">
            {[
              { icon: Swords, value: SEASON_STATS.matches, label: "matchs officiels", to: "/resultats" as const },
              { icon: Trophy, value: `${SEASON_STATS.winRate}%`, label: "de victoires", to: "/resultats" as const },
              { icon: Users, value: PLAYERS.length, label: "joueurs EVA sous contrat", to: "/equipe" as const },
            ].map((s, i) => (
              <Reveal key={s.label} delay={i * 100}>
                <Link to={s.to} className="block">
                  <TiltCard className="panel clip-blade p-7">
                    <s.icon className="tilt-pop size-6 text-primary" />
                    <p className="tilt-pop-lg mt-6 font-display text-6xl font-black text-gold">{s.value}</p>
                    <p className="tilt-pop mt-2 font-label text-xs font-semibold uppercase tracking-[0.22em] text-muted-foreground">
                      {s.label}
                    </p>
                  </TiltCard>
                </Link>
              </Reveal>
            ))}
          </div>
        </Container>
      </section>

      {/* MAILLOT */}
      <section className="relative py-24">
        <Container className="grid items-center gap-12 lg:grid-cols-[1fr_1.1fr]">
          <Reveal className="order-2 lg:order-1">
            <p className="eyebrow mb-4">Official merch • 2026</p>
            <h2 className="font-display text-4xl font-black uppercase sm:text-5xl">
              Maillot officiel <span className="text-gold">2026</span>
            </h2>
            <p className="mt-4 font-display text-3xl font-bold text-primary">{PRODUCTS.jersey.price}</p>
            <p className="mt-5 max-w-md text-lg text-muted-foreground">
              Portez nos couleurs. Le maillot officiel et les manchettes Elysium 2026 sont disponibles dès maintenant.
            </p>
            <Button asChild variant="gold" size="xl" className="mt-8">
              <Link to="/boutique">
                Voir la boutique <ArrowRight />
              </Link>
            </Button>
          </Reveal>
          <Reveal className="order-1 lg:order-2" delay={100}>
            <TiltCard max={9} className="clip-blade overflow-hidden shadow-gold">
              <img
                src={PRODUCTS.jersey.image}
                alt="Maillot officiel Elysium Esport 2026"
                className="aspect-[4/3] w-full object-cover"
                loading="lazy"
              />
            </TiltCard>
          </Reveal>
        </Container>
      </section>

      {/* EN DIRECT + COMMUNAUTÉ */}
      <section className="relative py-20">
        <Container className="grid gap-6 lg:grid-cols-2">
          <Reveal>
            <TiltCard max={5} className="panel clip-blade h-full p-8">
              <p className="eyebrow mb-3">En direct</p>
              <h3 className="font-display text-3xl font-bold uppercase">Matchs & replays</h3>
              <p className="mt-3 text-muted-foreground">
                Suis nos matchs en live sur Twitch et retrouve les replays sur YouTube.
              </p>
              <div className="mt-8 flex flex-col gap-3 sm:flex-row">
                <Button asChild variant="gold" size="xl">
                  <a href={TWITCH_URL} target="_blank" rel="noreferrer">
                    Regarder sur Twitch
                  </a>
                </Button>
                <Button asChild variant="frame" size="xl">
                  <a href={YOUTUBE_URL} target="_blank" rel="noreferrer">
                    Chaîne YouTube
                  </a>
                </Button>
              </div>
            </TiltCard>
          </Reveal>
          <Reveal delay={120}>
            <TiltCard max={5} className="panel clip-blade h-full p-8">
              <p className="eyebrow mb-3">Communauté</p>
              <h3 className="font-display text-3xl font-bold uppercase">Rejoins le Discord</h3>
              <ul className="mt-5 space-y-3 text-muted-foreground">
                {[
                  "Respect avant tout — aucune insulte, haine ou harcèlement.",
                  "Pas de spam ni d'autopromotion sans accord du staff.",
                  "Utilise les bons salons et reste courtois en vocal.",
                ].map((rule, i) => (
                  <li key={rule} className="flex gap-3">
                    <span className="font-label text-sm font-bold text-primary">0{i + 1}</span>
                    {rule}
                  </li>
                ))}
              </ul>
              <Button asChild variant="gold" size="xl" className="mt-8">
                <a href={DISCORD_URL} target="_blank" rel="noreferrer">
                  Rejoindre le Discord
                </a>
              </Button>
            </TiltCard>
          </Reveal>
        </Container>
        <Container className="mt-10">
          <Reveal className="flex flex-wrap gap-3">
            {SOCIALS.map((s) => (
              <a
                key={s.label}
                href={s.href}
                target="_blank"
                rel="noreferrer"
                className="clip-blade-sm border border-border bg-background/50 px-5 py-3 font-label text-xs font-semibold uppercase tracking-[0.22em] text-muted-foreground backdrop-blur transition-colors hover:border-primary hover:text-primary"
              >
                {s.label}
              </a>
            ))}
          </Reveal>
        </Container>
      </section>

      {/* SOUTENIR */}
      <section className="relative py-24">
        <Container>
          <Reveal>
            <TiltCard max={4} className="panel clip-blade overflow-hidden p-8 text-center sm:p-16">
              <img src={BRAND.brackets} alt="" className="pointer-events-none absolute -left-10 top-1/2 hidden h-72 -translate-y-1/2 opacity-20 sm:block" loading="lazy" />
              <p className="eyebrow mb-4">Soutien à l'association</p>
              <h2 className="tilt-pop font-display text-4xl font-black uppercase sm:text-6xl">
                Portez nos couleurs <span className="text-gold">plus loin</span>
              </h2>
              <p className="mx-auto mt-5 max-w-xl text-lg text-muted-foreground">
                Un don, même modeste, finance le matériel, les déplacements et les inscriptions de nos rosters EVA et Rocket
                League.
              </p>
              <Button asChild variant="gold" size="xl" className="mt-9">
                <Link to="/soutenir">
                  Soutenir Elysium <ArrowRight />
                </Link>
              </Button>
            </TiltCard>
          </Reveal>
        </Container>
      </section>
    </>
  );
}
