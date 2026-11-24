import { createFileRoute } from "@tanstack/react-router";
import { ArrowRight } from "lucide-react";

import { Container, PageHero, SectionHeading } from "@/components/site/Headings";
import { Reveal } from "@/components/site/Reveal";
import { TiltCard } from "@/components/site/TiltCard";
import { Button } from "@/components/ui/button";
import { DISCORD_URL, RECRUITMENT_STEPS } from "@/lib/elysium-data";

export const Route = createFileRoute("/recrutement")({
  head: () => ({
    meta: [
      { title: "Recrutement — Elysium Esport" },
      { name: "description", content: "Tu penses avoir le niveau ? Prouve-le. Rejoins les rosters EVA et Rocket League d'Elysium." },
      { property: "og:title", content: "Recrutement — Elysium Esport" },
      { property: "og:description", content: "Tu penses avoir le niveau ? Prouve-le. Rejoins Elysium." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: RecruitmentPage,
});

function RecruitmentPage() {
  return (
    <>
      <PageHero eyebrow="Rejoindre Elysium" title="Prouve-le" subtitle="Tu penses avoir le niveau ? Rejoins Elysium.">
        <Button asChild variant="gold" size="xl">
          <a href={DISCORD_URL} target="_blank" rel="noreferrer">
            Candidater sur Discord <ArrowRight />
          </a>
        </Button>
      </PageHero>

      <section className="relative py-12">
        <Container>
          <Reveal>
            <div className="panel clip-blade p-8 sm:p-10">
              <p className="eyebrow mb-3">Postes ouverts</p>
              <p className="font-display text-2xl font-bold uppercase">Candidatures spontanées bienvenues</p>
              <p className="mt-2 text-muted-foreground">
                Aucun poste ouvert pour le moment — envoie-nous ton profil, tes VOD et tes disponibilités.
              </p>
            </div>
          </Reveal>
        </Container>
      </section>

      <section className="relative py-20">
        <Container>
          <Reveal>
            <SectionHeading eyebrow="Du dossier à la décision" title={<>Comment se déroule <span className="text-gold">une candidature</span></>} />
          </Reveal>
          <div className="relative grid gap-6 md:grid-cols-4">
            <div className="gold-line absolute left-0 right-0 top-16 hidden md:block" />
            {RECRUITMENT_STEPS.map((s, i) => (
              <Reveal key={s.title} delay={i * 140}>
                <TiltCard max={12} className="panel clip-blade h-full p-7">
                  <p className="tilt-pop-lg font-display text-6xl font-black text-outline">0{i + 1}</p>
                  <h3 className="tilt-pop mt-4 font-display text-xl font-bold uppercase">{s.title}</h3>
                  <p className="mt-2 text-muted-foreground">{s.text}</p>
                  <p className="mt-5 font-label text-xs font-semibold uppercase tracking-[0.22em] text-primary">
                    {s.days} jours en moyenne
                  </p>
                </TiltCard>
              </Reveal>
            ))}
          </div>
          <p className="mt-6 text-sm text-muted-foreground">
            Délais observés en moyenne : un dossier complet les respecte presque toujours.
          </p>
        </Container>
      </section>

      <section className="relative py-12">
        <Container>
          <Reveal>
            <TiltCard max={4} className="panel clip-blade p-8 text-center sm:p-14">
              <p className="eyebrow mb-3">Tryouts & essais ouverts</p>
              <h2 className="tilt-pop font-display text-3xl font-black uppercase sm:text-5xl">
                Rien n'est donné. <span className="text-gold">Tout se mérite.</span>
              </h2>
              <p className="mx-auto mt-4 max-w-lg text-muted-foreground">
                Aucun créneau d'essai planifié pour le moment — les candidatures spontanées restent ouvertes. Les prochains
                tryouts sont annoncés sur le Discord.
              </p>
              <Button asChild variant="gold" size="xl" className="mt-8">
                <a href={DISCORD_URL} target="_blank" rel="noreferrer">
                  Rejoindre le Discord
                </a>
              </Button>
            </TiltCard>
          </Reveal>
        </Container>
      </section>
    </>
  );
}
